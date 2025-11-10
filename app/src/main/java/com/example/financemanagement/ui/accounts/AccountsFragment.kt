package com.example.financemanagement.ui.accounts

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financemanagement.R
import com.example.financemanagement.databinding.FragmentSavingGoalsBinding
import com.example.financemanagement.databinding.DialogCreateGoalBinding
import com.example.financemanagement.databinding.DialogAddContributionBinding
import com.example.financemanagement.databinding.DialogEditGoalBinding
import com.example.financemanagement.domain.model.SavingGoal
import com.example.financemanagement.utils.CurrencyTextWatcher
import com.example.financemanagement.viewmodel.SavingGoalViewModel
import com.example.financemanagement.viewmodel.SavingGoalUiState
import com.example.financemanagement.viewmodel.CreateGoalUiState
import com.example.financemanagement.viewmodel.ContributionUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AccountsFragment : Fragment() {

    private var _binding: FragmentSavingGoalsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SavingGoalViewModel by viewModels()
    private lateinit var adapter: SavingGoalsAdapter
    
    private var selectedDate = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavingGoalsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = SavingGoalsAdapter(
            onAddContributionClick = { goal ->
                showAddContributionDialog(goal)
            },
            onViewDetailsClick = { goal ->
                // Navigate to goal details
                val bundle = Bundle().apply {
                    putString("goalId", goal.id)
                }
                findNavController().navigate(R.id.action_accountsFragment_to_goalDetailsFragment, bundle)
            },
            onMenuClick = { goal, view ->
                showGoalMenu(goal, view)
            }
        )
        
        binding.rvSavingGoals.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@AccountsFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddGoal.setOnClickListener {
            showCreateGoalDialog()
        }
        
        binding.btnCreateFirstGoal.setOnClickListener {
            showCreateGoalDialog()
        }
        
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadSavingGoals()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                handleUiState(state)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createGoalState.collect { state ->
                handleCreateGoalState(state)
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contributionState.collect { state ->
                handleContributionState(state)
            }
        }
    }

    private fun handleUiState(state: SavingGoalUiState) {
        when (state) {
            is SavingGoalUiState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvSavingGoals.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
            is SavingGoalUiState.Success -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                
                if (state.goals.isEmpty()) {
                    binding.layoutEmptyState.visibility = View.VISIBLE
                    binding.rvSavingGoals.visibility = View.GONE
                } else {
                    binding.layoutEmptyState.visibility = View.GONE
                    binding.rvSavingGoals.visibility = View.VISIBLE
                    adapter.submitList(state.goals)
                }
            }
            is SavingGoalUiState.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleCreateGoalState(state: CreateGoalUiState) {
        when (state) {
            is CreateGoalUiState.Success -> {
                Toast.makeText(context, "Goal created successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetCreateGoalState()
            }
            is CreateGoalUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetCreateGoalState()
            }
            else -> {}
        }
    }

    private fun handleContributionState(state: ContributionUiState) {
        when (state) {
            is ContributionUiState.Loading -> {
                // Could show a progress dialog or disable buttons
            }
            is ContributionUiState.Success -> {
                Toast.makeText(context, "Contribution added successfully!", Toast.LENGTH_SHORT).show()
                viewModel.resetContributionState()
            }
            is ContributionUiState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                viewModel.resetContributionState()
            }
            else -> {}
        }
    }

    private fun showCreateGoalDialog() {
        val dialogBinding = DialogCreateGoalBinding.inflate(layoutInflater)
        
        // Add currency formatter to target amount field
        dialogBinding.etTargetAmount.addTextChangedListener(CurrencyTextWatcher(dialogBinding.etTargetAmount))
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.etTargetDate.setOnClickListener {
            showDatePicker { date ->
                val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                dialogBinding.etTargetDate.setText(formatter.format(date))
            }
        }
        
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnCreate.setOnClickListener {
            val title = dialogBinding.etGoalTitle.text.toString().trim()
            val description = dialogBinding.etGoalDescription.text.toString().trim().takeIf { it.isNotEmpty() }
            val amountText = dialogBinding.etTargetAmount.text.toString().trim()
            
            // Reset errors
            dialogBinding.etGoalTitle.error = null
            dialogBinding.etTargetAmount.error = null
            
            when {
                title.isEmpty() -> {
                    dialogBinding.etGoalTitle.error = "Title is required"
                    return@setOnClickListener
                }
                title.length < 3 -> {
                    dialogBinding.etGoalTitle.error = "Title must be at least 3 characters"
                    return@setOnClickListener
                }
                amountText.isEmpty() -> {
                    dialogBinding.etTargetAmount.error = "Target amount is required"
                    return@setOnClickListener
                }
                dialogBinding.etTargetDate.text.toString().isEmpty() -> {
                    Toast.makeText(context, "Please select a target date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            
            val amount = CurrencyTextWatcher.parseCurrency(amountText)
            if (amount == null || amount <= 0) {
                dialogBinding.etTargetAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }
            
            if (amount < 1) {
                dialogBinding.etTargetAmount.error = "Amount must be at least $1"
                return@setOnClickListener
            }
            
            if (amount > 999999999) {
                dialogBinding.etTargetAmount.error = "Amount is too large"
                return@setOnClickListener
            }
            
            Log.d("AccountsFragment", "Creating goal: title=$title, amount=$amount, date=${selectedDate.time}")
            viewModel.createSavingGoal(title, description, amount, selectedDate.time)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun showAddContributionDialog(goal: SavingGoal) {
        val dialogBinding = DialogAddContributionBinding.inflate(layoutInflater)
        dialogBinding.tvGoalInfo.text = "To: ${goal.title}"
        
        // Add currency formatter to amount field
        dialogBinding.etAmount.addTextChangedListener(CurrencyTextWatcher(dialogBinding.etAmount))
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnAdd.setOnClickListener {
            val amountText = dialogBinding.etAmount.text.toString().trim()
            val note = dialogBinding.etNote.text.toString().trim().takeIf { it.isNotEmpty() }
            
            if (amountText.isEmpty()) {
                dialogBinding.etAmount.error = "Amount is required"
                return@setOnClickListener
            }
            
            val amount = CurrencyTextWatcher.parseCurrency(amountText)
            if (amount == null || amount <= 0) {
                dialogBinding.etAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }
            
            // Check if amount exceeds remaining amount needed
            val remainingAmount = goal.goalAmount - goal.currentAmount
            if (amount > remainingAmount) {
                val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
                dialogBinding.etAmount.error = "Max allowed: ${currencyFormat.format(remainingAmount)} đ"
                return@setOnClickListener
            }
            
            Log.d("AccountsFragment", "Adding contribution: goalId=${goal.id}, amount=$amount, note=$note")
            viewModel.addContribution(goal.id, amount, note)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun showGoalMenu(goal: SavingGoal, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_goal_options, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    // Open edit dialog
                    showEditGoalDialog(goal)
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation(goal)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }

    private fun showDeleteConfirmation(goal: SavingGoal) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete \"${goal.title}\"? This will also delete all contributions.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGoal(goal.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditGoalDialog(goal: SavingGoal) {
        val dialogBinding = DialogEditGoalBinding.inflate(layoutInflater)

        // Pre-fill current data
        dialogBinding.etGoalTitle.setText(goal.title)
        dialogBinding.etGoalDescription.setText(goal.description ?: "")

        // Add currency formatter to target amount field
        dialogBinding.etTargetAmount.addTextChangedListener(CurrencyTextWatcher(dialogBinding.etTargetAmount))

        // Set current target amount (formatted)
        val amountText = goal.goalAmount.toLong().toString()
        dialogBinding.etTargetAmount.setText(amountText)

        // Parse and set target date
        val inputDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayDateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        var localSelectedDate = Calendar.getInstance()

        try {
            val parsedDate = inputDateFormatter.parse(goal.goalDate)
            if (parsedDate != null) {
                localSelectedDate.time = parsedDate
                dialogBinding.etTargetDate.setText(displayDateFormatter.format(parsedDate))
            }
        } catch (e: Exception) {
            dialogBinding.etTargetDate.setText(goal.goalDate)
        }

        // Date picker
        dialogBinding.etTargetDate.setOnClickListener {
            android.app.DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    localSelectedDate.set(year, month, dayOfMonth)
                    dialogBinding.etTargetDate.setText(displayDateFormatter.format(localSelectedDate.time))
                },
                localSelectedDate.get(Calendar.YEAR),
                localSelectedDate.get(Calendar.MONTH),
                localSelectedDate.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.minDate = System.currentTimeMillis()
            }.show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val title = dialogBinding.etGoalTitle.text.toString().trim()
            val description = dialogBinding.etGoalDescription.text.toString().trim().takeIf { it.isNotEmpty() }
            val amountText2 = dialogBinding.etTargetAmount.text.toString().trim()

            // Reset errors
            dialogBinding.etGoalTitle.error = null
            dialogBinding.etTargetAmount.error = null

            when {
                title.isEmpty() -> {
                    dialogBinding.etGoalTitle.error = "Title is required"
                    return@setOnClickListener
                }
                title.length < 3 -> {
                    dialogBinding.etGoalTitle.error = "Title must be at least 3 characters"
                    return@setOnClickListener
                }
                amountText2.isEmpty() -> {
                    dialogBinding.etTargetAmount.error = "Target amount is required"
                    return@setOnClickListener
                }
                dialogBinding.etTargetDate.text.toString().isEmpty() -> {
                    Toast.makeText(context, "Please select a target date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            val amount = CurrencyTextWatcher.parseCurrency(amountText2)
            if (amount == null || amount <= 0) {
                dialogBinding.etTargetAmount.error = "Please enter a valid amount"
                return@setOnClickListener
            }

            if (amount < 1) {
                dialogBinding.etTargetAmount.error = "Amount must be at least $1"
                return@setOnClickListener
            }

            // Validate: new goal amount should not be less than current contributed amount
            if (amount < goal.currentAmount) {
                val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
                dialogBinding.etTargetAmount.error = "Cannot be less than contributed amount (${currencyFormat.format(goal.currentAmount)} đ)"
                return@setOnClickListener
            }

            Log.d("AccountsFragment", "Updating goal: id=${goal.id}, title=$title, amount=$amount, currentAmount=${goal.currentAmount}")

            // Ensure createdAt fallback
            val isoNow = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
            viewModel.updateSavingGoal(
                goalId = goal.id,
                title = title,
                description = description,
                goalAmount = amount,
                targetDate = localSelectedDate.time,
                currentAmount = goal.currentAmount,
                createdAt = goal.createdAt ?: isoNow
            )

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                onDateSelected(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = System.currentTimeMillis()
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
