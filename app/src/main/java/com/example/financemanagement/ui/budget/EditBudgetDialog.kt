package com.example.financemanagement.ui.budget

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.financemanagement.databinding.DialogEditBudgetBinding
import java.text.SimpleDateFormat
import java.util.*

class EditBudgetDialog(
    private val budgetItem: BudgetItem,
    private val onSave: (budgetId: String?, categoryId: String, newLimitAmount: Double, month: Int, year: Int) -> Unit
) : DialogFragment() {

    private var _binding: DialogEditBudgetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        setupClickListeners()
    }

    private fun setupViews() {
        // Display category info
        binding.tvCategoryIcon.text = budgetItem.categoryIcon
        binding.tvCategoryName.text = budgetItem.categoryName
        
        // Set current limit amount
        val currentAmount = if (budgetItem.limitAmount > 0) {
            budgetItem.limitAmount.toInt().toString()
        } else {
            "" // Empty if not set yet
        }
        binding.etLimitAmount.setText(currentAmount)
        
        // Display month and year
        val monthName = getMonthName(budgetItem.month)
        binding.tvMonthYear.text = "For: $monthName ${budgetItem.year}"
        
        // Select all text when focused
        binding.etLimitAmount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.etLimitAmount.selectAll()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSave.setOnClickListener {
            val limitAmountStr = binding.etLimitAmount.text.toString().trim()
            
            if (limitAmountStr.isEmpty()) {
                binding.tilLimitAmount.error = "Please enter limit amount"
                return@setOnClickListener
            }
            
            val limitAmount = limitAmountStr.toDoubleOrNull()
            
            if (limitAmount == null || limitAmount <= 0) {
                binding.tilLimitAmount.error = "Please enter a valid amount greater than 0"
                return@setOnClickListener
            }

            // Clear error and save
            binding.tilLimitAmount.error = null
            onSave(
                budgetItem.budgetId, 
                budgetItem.category.id, 
                limitAmount,
                budgetItem.month,
                budgetItem.year
            )
            dismiss()
        }

        // Clear error when user starts typing
        binding.etLimitAmount.setOnFocusChangeListener { _, _ ->
            binding.tilLimitAmount.error = null
        }
    }

    private fun getMonthName(month: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month - 1) // Month is 1-based in API
        val format = SimpleDateFormat("MMMM", Locale.getDefault())
        return format.format(calendar.time)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
