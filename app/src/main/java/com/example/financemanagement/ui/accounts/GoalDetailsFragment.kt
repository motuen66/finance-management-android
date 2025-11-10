package com.example.financemanagement.ui.accounts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financemanagement.databinding.FragmentGoalDetailsBinding
import com.example.financemanagement.databinding.DialogAddContributionBinding
import com.example.financemanagement.databinding.DialogEditGoalBinding
import com.example.financemanagement.domain.model.SavingGoal
import com.example.financemanagement.utils.CurrencyTextWatcher
import com.example.financemanagement.viewmodel.SavingGoalViewModel
import com.example.financemanagement.viewmodel.SavingGoalUiState
import com.example.financemanagement.viewmodel.ContributionUiState
import com.example.financemanagement.viewmodel.EditGoalUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class GoalDetailsFragment : Fragment() {

    private var _binding: FragmentGoalDetailsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: SavingGoalViewModel by viewModels()
    private lateinit var contributionsAdapter: ContributionsAdapter
    
    private var currentGoal: SavingGoal? = null
    private var goalId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        goalId = arguments?.getString("goalId") ?: ""
        if (goalId.isEmpty()) {
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGoalDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // Load data
        loadGoalDetails()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        contributionsAdapter = ContributionsAdapter { contributionId ->
            showDeleteContributionConfirmation(contributionId)
        }
        
        binding.rvContributions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contributionsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnAddContribution.setOnClickListener {
            currentGoal?.let { goal ->
                showAddContributionDialog(goal)
            }
        }
        
        binding.btnEditGoal.setOnClickListener {
            currentGoal?.let { goal ->
                showEditGoalDialog(goal)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SavingGoalUiState.Success -> {
                        val goal = state.goals.find { it.id == goalId }
                        if (goal != null) {
                            currentGoal = goal
                            updateUI(goal)
                        }
                    }
                    else -> {}
                }
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedGoalContributions.collect { contributions ->
                contributionsAdapter.submitList(contributions)
                binding.layoutEmptyContributions.visibility = 
                    if (contributions.isEmpty()) View.VISIBLE else View.GONE
            }
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.contributionState.collect { state ->
                when (state) {
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
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.editGoalState.collect { state ->
                when (state) {
                    is EditGoalUiState.Success -> {
                        Toast.makeText(context, "Goal updated successfully!", Toast.LENGTH_SHORT).show()
                        viewModel.resetEditGoalState()
                    }
                    is EditGoalUiState.Error -> {
                        Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                        viewModel.resetEditGoalState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun loadGoalDetails() {
        viewModel.loadSavingGoals()
        viewModel.loadContributionsForGoal(goalId)
        
        // Also observe real-time contributions
        viewModel.observeContributionsForGoal(goalId)
    }

    private fun updateUI(goal: SavingGoal) {
        binding.apply {
            // Goal info
            tvGoalTitle.text = goal.title
            tvGoalDescription.apply {
                if (!goal.description.isNullOrEmpty()) {
                    text = goal.description
                    visibility = View.VISIBLE
                } else {
                    visibility = View.GONE
                }
            }
            
            // Progress
            val progressPercentage = (goal.progress * 100).toInt()
            Log.d("GoalDetailsFragment", "Updating progress: currentAmount=${goal.currentAmount}, goalAmount=${goal.goalAmount}, progress=${goal.progress}, percentage=$progressPercentage%")
            // Set percentage text and progress bar value
            tvProgressPercentage.text = "$progressPercentage%"
            try {
                progressBar.progress = progressPercentage
            } catch (e: Exception) {
                // Ignore if progressBar not present (defensive)
            }
            
            // Amounts
            val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
            tvCurrentAmount.text = currencyFormat.format(goal.currentAmount)
            tvTargetAmount.text = "of ${currencyFormat.format(goal.goalAmount)}"
            
            // Target date
            val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            try {
                val targetDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val targetDate = targetDateFormatter.parse(goal.goalDate)
                tvTargetDate.text = if (targetDate != null) {
                    "Target: ${dateFormatter.format(targetDate)}"
                } else {
                    "Target: ${goal.goalDate}"
                }
            } catch (e: Exception) {
                tvTargetDate.text = "Target: ${goal.goalDate}"
            }
            
            // Update toolbar title
            toolbar.title = goal.title
            
            // Disable add contribution if goal is completed
            btnAddContribution.isEnabled = !goal.isAchieved
            // Edit should be allowed unless goal is completed; visually indicate disabled state
            btnEditGoal.isEnabled = !goal.isAchieved
            btnEditGoal.alpha = if (btnEditGoal.isEnabled) 1.0f else 0.5f
        }
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
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                dialogBinding.etAmount.error = "Max allowed: ${currencyFormat.format(remainingAmount)}"
                return@setOnClickListener
            }
            
            Log.d("GoalDetailsFragment", "Adding contribution: goalId=${goal.id}, amount=$amount, note=$note")
            viewModel.addContribution(goal.id, amount, note)
            dialog.dismiss()
        }
        
        dialog.show()
    }

    private fun showDeleteContributionConfirmation(contributionId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Contribution")
            .setMessage("Are you sure you want to delete this contribution?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteContribution(contributionId, goalId)
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
        var selectedDate = Calendar.getInstance()
        
        try {
            val parsedDate = inputDateFormatter.parse(goal.goalDate)
            if (parsedDate != null) {
                selectedDate.time = parsedDate
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
                    selectedDate.set(year, month, dayOfMonth)
                    dialogBinding.etTargetDate.setText(displayDateFormatter.format(selectedDate.time))
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
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
            
            // Validate: new goal amount should not be less than current contributed amount
            if (amount < goal.currentAmount) {
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                dialogBinding.etTargetAmount.error = "Cannot be less than contributed amount (${currencyFormat.format(goal.currentAmount)})"
                return@setOnClickListener
            }
            
            Log.d("GoalDetailsFragment", "Updating goal: id=${goal.id}, title=$title, amount=$amount, currentAmount=${goal.currentAmount}")
            // Ensure createdAt is non-null when calling update (fallback to now if missing)
            val isoNow = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).format(java.util.Date())
            viewModel.updateSavingGoal(
                goalId = goal.id,
                title = title,
                description = description,
                goalAmount = amount,
                targetDate = selectedDate.time,
                currentAmount = goal.currentAmount, // Giữ nguyên số tiền đã contribute
                createdAt = goal.createdAt ?: isoNow
            )
            dialog.dismiss()
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}