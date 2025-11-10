package com.example.financemanagement.ui.home

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentHomeAddExpenseBinding
import com.example.financemanagement.viewmodel.HomeAddExpenseViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

@AndroidEntryPoint
class HomeAddExpenseFragment : Fragment() {

    private var _binding: FragmentHomeAddExpenseBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeAddExpenseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        android.util.Log.d("HomeAddExpenseFragment", "onViewCreated called")

        // Date picker setup
        setUpDatePicker()

        // Category selection
        // Setup inline budgets RecyclerView
        val inlineBudgetList = binding.inlineBudgetList
        val budgetAdapter = BudgetAdapter { selectedBudget ->
            // when user selects a budget from the inline list, set the category field and hide list
            binding.categorySelectField.setText(selectedBudget.categoryName)
            inlineBudgetList.visibility = View.GONE
            // Save selected categoryId
            viewModel.setSelectedCategory(selectedBudget.categoryId)
        }
        inlineBudgetList.layoutManager = LinearLayoutManager(requireContext())
        inlineBudgetList.adapter = budgetAdapter

        // Show inline list when category field is clicked; fetch budgets if needed
        binding.categorySelectField.setOnClickListener {
            android.util.Log.d("HomeAddExpenseFragment", "categorySelectField clicked")
            // toggle visibility
            if (inlineBudgetList.visibility == View.VISIBLE) {
                inlineBudgetList.visibility = View.GONE
            } else {
                inlineBudgetList.visibility = View.VISIBLE
                viewModel.fetchBudgets()
            }
        }

        // Collect budgets and submit to adapter
        lifecycleScope.launchWhenStarted {
            viewModel.budgets.collectLatest { budgets ->
                android.util.Log.d("HomeAddExpenseFragment", "Collected budgets: ${budgets.size}")
                budgetAdapter.submitList(budgets)
                // show/hide list depending on data
                if (budgets.isEmpty()) {
                    inlineBudgetList.visibility = View.GONE
                } else if (binding.inlineBudgetList.visibility == View.VISIBLE) {
                    inlineBudgetList.visibility = View.VISIBLE
                }
            }
        }

        // Save button click handler
        binding.submitExpenseBtn.setOnClickListener {
            handleSaveTransaction()
        }

        // Collect transaction creation result
        lifecycleScope.launchWhenStarted {
            viewModel.transactionCreated.collectLatest { transaction ->
                if (transaction != null) {
                    android.util.Log.d("HomeAddExpenseFragment", "Transaction created successfully: ${transaction.id}")
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Transaction created successfully",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    // Navigate back to HomeFragment
                    findNavController().popBackStack()
                }
            }
        }

        // Collect errors
        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest { error ->
                if (error != null) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        "Error: $error",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Auto-fetch budgets once on screen open to verify call path and populate cache
        viewModel.fetchBudgets()
    }

    private fun setUpDatePicker() {
        // Setup datePicker
        val dateInputField = binding.dateInputField
        val calendar = Calendar.getInstance()
        val today = "%02d/%02d/%04d".format(
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )

        // Set today as default
        dateInputField.setText(today)
        dateInputField.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "%02d/%02d/%04d".format(selectedDay, selectedMonth + 1, selectedYear)
                dateInputField.setText(formattedDate)
            }, year, month, day)

            datePicker.show()
        }
    }

    private fun handleSaveTransaction() {
        android.util.Log.d("HomeAddExpenseFragment", "Save button clicked")

        // Get form values
        val dateText = binding.dateInputField.text.toString().trim()
        val amountText = binding.amountInputField.text.toString().trim()
        val noteText = binding.noteInputField.text.toString().trim()
        val categoryId = viewModel.selectedCategoryId.value

        // Validate inputs
        if (dateText.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "Please select a date", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (amountText.isEmpty()) {
            android.widget.Toast.makeText(requireContext(), "Please enter an amount", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        if (categoryId.isNullOrEmpty()) {
            android.widget.Toast.makeText(requireContext(), "Please select a category", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            android.widget.Toast.makeText(requireContext(), "Please enter a valid amount", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        // Convert date from DD/MM/YYYY to ISO 8601 format (YYYY-MM-DDTHH:mm:ss.sssZ)
        val isoDate = convertToIsoDate(dateText)
        if (isoDate == null) {
            android.widget.Toast.makeText(requireContext(), "Invalid date format", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        android.util.Log.d("HomeAddExpenseFragment", "Creating transaction: date=$isoDate, amount=$amount, note=$noteText, categoryId=$categoryId")

        // Call ViewModel to create transaction
        viewModel.createTransaction(
            note = noteText.ifEmpty { "No note" }, // Backend requires note, provide default if empty
            amount = amount,
            date = isoDate,
            type = "Expense",
            categoryId = categoryId
        )
    }

    private fun convertToIsoDate(dateStr: String): String? {
        return try {
            // Parse DD/MM/YYYY
            val parts = dateStr.split("/")
            if (parts.size != 3) return null

            val day = parts[0].toInt()
            val month = parts[1].toInt()
            val year = parts[2].toInt()

            val calendar = Calendar.getInstance()
            calendar.set(year, month - 1, day, 0, 0, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Format to ISO 8601
            val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            isoFormat.format(calendar.time)
        } catch (e: Exception) {
            android.util.Log.e("HomeAddExpenseFragment", "Error converting date", e)
            null
        }
    }

    private fun clearForm() {
        binding.amountInputField.text?.clear()
        binding.noteInputField.text?.clear()
        binding.categorySelectField.text?.clear()
        viewModel.setSelectedCategory("")
        
        // Reset date to today
        val calendar = Calendar.getInstance()
        val today = "%02d/%02d/%04d".format(
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR)
        )
        binding.dateInputField.setText(today)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}