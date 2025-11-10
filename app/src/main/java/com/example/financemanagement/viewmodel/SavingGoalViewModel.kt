package com.example.financemanagement.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.repository.SavingGoalRepository
import com.example.financemanagement.domain.model.SavingGoal
import com.example.financemanagement.domain.model.SavingContribution
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed interface SavingGoalUiState {
    object Loading : SavingGoalUiState
    data class Success(val goals: List<SavingGoal>) : SavingGoalUiState
    data class Error(val message: String) : SavingGoalUiState
}

sealed interface CreateGoalUiState {
    object Idle : CreateGoalUiState
    object Loading : CreateGoalUiState
    object Success : CreateGoalUiState
    data class Error(val message: String) : CreateGoalUiState
}

sealed interface ContributionUiState {
    object Idle : ContributionUiState
    object Loading : ContributionUiState
    object Success : ContributionUiState
    data class Error(val message: String) : ContributionUiState
}

sealed interface EditGoalUiState {
    object Idle : EditGoalUiState
    object Loading : EditGoalUiState
    object Success : EditGoalUiState
    data class Error(val message: String) : EditGoalUiState
}

@HiltViewModel
class SavingGoalViewModel @Inject constructor(
    private val repository: SavingGoalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SavingGoalUiState>(SavingGoalUiState.Loading)
    val uiState: StateFlow<SavingGoalUiState> = _uiState.asStateFlow()

    private val _createGoalState = MutableStateFlow<CreateGoalUiState>(CreateGoalUiState.Idle)
    val createGoalState: StateFlow<CreateGoalUiState> = _createGoalState.asStateFlow()

    private val _contributionState = MutableStateFlow<ContributionUiState>(ContributionUiState.Idle)
    val contributionState: StateFlow<ContributionUiState> = _contributionState.asStateFlow()

    private val _editGoalState = MutableStateFlow<EditGoalUiState>(EditGoalUiState.Idle)
    val editGoalState: StateFlow<EditGoalUiState> = _editGoalState.asStateFlow()

    private val _selectedGoalContributions = MutableStateFlow<List<SavingContribution>>(emptyList())
    val selectedGoalContributions: StateFlow<List<SavingContribution>> = _selectedGoalContributions.asStateFlow()

    init {
        observeSavingGoals()
        // Load initial data from server
        viewModelScope.launch {
            loadSavingGoals()
        }
    }

    private fun observeSavingGoals() {
        viewModelScope.launch {
            repository.observeSavingGoals()
                .catch { e ->
                    _uiState.value = SavingGoalUiState.Error(e.message ?: "Unknown error")
                }
                .collect { goals ->
                    _uiState.value = SavingGoalUiState.Success(goals)
                }
        }
    }

    fun loadSavingGoals() {
        viewModelScope.launch {
            _uiState.value = SavingGoalUiState.Loading
            repository.getSavingGoals()
                .onSuccess { goals ->
                    _uiState.value = SavingGoalUiState.Success(goals)
                }
                .onFailure { error ->
                    _uiState.value = SavingGoalUiState.Error(error.message ?: "Failed to load goals")
                }
        }
    }

    fun createSavingGoal(
        title: String,
        description: String?,
        goalAmount: Double,
        targetDate: Date
    ) {
        viewModelScope.launch {
            Log.d("SavingGoalVM", "Creating goal: title=$title, amount=$goalAmount")
            _createGoalState.value = CreateGoalUiState.Loading
            
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            
            val goal = SavingGoal(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                goalAmount = goalAmount,
                currentAmount = 0.0,
                goalDate = dateFormatter.format(targetDate),
                createdAt = isoFormatter.format(Date()),
                isCompleted = false
            )
            
            Log.d("SavingGoalVM", "Created goal object: $goal")
            
            repository.createSavingGoal(goal)
                .onSuccess {
                    Log.d("SavingGoalVM", "Goal created successfully")
                    _createGoalState.value = CreateGoalUiState.Success
                    // Rely on Flow observers to update UI automatically
                }
                .onFailure { error ->
                    Log.e("SavingGoalVM", "Failed to create goal", error)
                    _createGoalState.value = CreateGoalUiState.Error(error.message ?: "Failed to create goal")
                }
        }
    }

    fun addContribution(
        goalId: String,
        amount: Double,
        note: String?
    ) {
        viewModelScope.launch {
            Log.d("SavingGoalVM", "Adding contribution: goalId=$goalId, amount=$amount, note=$note")
            _contributionState.value = ContributionUiState.Loading
            
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            
            val contribution = SavingContribution(
                id = UUID.randomUUID().toString(),
                goalId = goalId,
                amount = amount,
                note = note,
                createdAt = isoFormatter.format(Date())
            )
            
            Log.d("SavingGoalVM", "Created contribution object: $contribution")
            
            repository.addContribution(contribution)
                .onSuccess {
                    Log.d("SavingGoalVM", "Contribution added successfully")
                    _contributionState.value = ContributionUiState.Success
                    // Local DB already updated by repository.addContribution (and updateGoalProgress).
                    // Rely on observeSavingGoals() and observeContributionsForGoal() to emit updated data
                    // Avoid immediate remote refresh which may overwrite local progress with stale server data.
                }
                .onFailure { error ->
                    Log.e("SavingGoalVM", "Failed to add contribution", error)
                    _contributionState.value = ContributionUiState.Error(error.message ?: "Failed to add contribution")
                }
        }
    }

    fun loadContributionsForGoal(goalId: String) {
        viewModelScope.launch {
            repository.getContributionsByGoalId(goalId)
                .onSuccess { contributions ->
                    _selectedGoalContributions.value = contributions
                }
                .onFailure { error ->
                    // Handle error silently for now, or you can emit to a separate state
                }
        }
    }

    fun observeContributionsForGoal(goalId: String) {
        viewModelScope.launch {
            repository.observeContributionsByGoalId(goalId)
                .catch { /* Handle error */ }
                .collect { contributions ->
                    _selectedGoalContributions.value = contributions
                }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            repository.deleteSavingGoal(goalId)
                .onSuccess {
                    // Rely on Flow observers to update UI automatically
                }
                .onFailure { error ->
                    _uiState.value = SavingGoalUiState.Error(error.message ?: "Failed to delete goal")
                }
        }
    }

    fun deleteContribution(contributionId: String, goalId: String) {
        viewModelScope.launch {
            repository.deleteContribution(contributionId)
                .onSuccess {
                    // Update goal progress after deleting contribution
                    repository.updateGoalProgress(goalId)
                    // Rely on Flow observers to update UI automatically
                }
                .onFailure { error ->
                    _contributionState.value = ContributionUiState.Error(error.message ?: "Failed to delete contribution")
                }
        }
    }

    fun resetCreateGoalState() {
        _createGoalState.value = CreateGoalUiState.Idle
    }

    fun resetContributionState() {
        _contributionState.value = ContributionUiState.Idle
    }

    fun updateSavingGoal(
        goalId: String,
        title: String,
        description: String?,
        goalAmount: Double,
        targetDate: Date,
        currentAmount: Double,
        createdAt: String
    ) {
        viewModelScope.launch {
            Log.d("SavingGoalVM", "Updating goal: id=$goalId, title=$title, amount=$goalAmount")
            _editGoalState.value = EditGoalUiState.Loading
            
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            val goal = SavingGoal(
                id = goalId,
                title = title,
                description = description,
                goalAmount = goalAmount,
                currentAmount = currentAmount, // Giữ nguyên currentAmount
                goalDate = dateFormatter.format(targetDate),
                createdAt = createdAt, // Giữ nguyên createdAt
                isCompleted = currentAmount >= goalAmount
            )
            
            Log.d("SavingGoalVM", "Updated goal object: $goal")
            
            repository.updateSavingGoal(goal)
                .onSuccess {
                    Log.d("SavingGoalVM", "Goal updated successfully")
                    _editGoalState.value = EditGoalUiState.Success
                    loadSavingGoals() // Refresh the list
                }
                .onFailure { error ->
                    Log.e("SavingGoalVM", "Failed to update goal", error)
                    _editGoalState.value = EditGoalUiState.Error(error.message ?: "Failed to update goal")
                }
        }
    }

    fun resetEditGoalState() {
        _editGoalState.value = EditGoalUiState.Idle
    }
}