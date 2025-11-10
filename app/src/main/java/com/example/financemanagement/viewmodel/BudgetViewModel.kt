package com.example.financemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.remote.models.CategoryRequest
import com.example.financemanagement.data.repository.BudgetRepository
import com.example.financemanagement.data.repository.CategoryRepository
import com.example.financemanagement.domain.model.Budget
import com.example.financemanagement.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BudgetUiState {
    object Idle : BudgetUiState
    object Loading : BudgetUiState
    data class Success(val budgets: List<Budget>, val categories: List<Category>) : BudgetUiState
    data class Error(val message: String) : BudgetUiState
}

sealed interface CategoryCreationState {
    object Idle : CategoryCreationState
    object Creating : CategoryCreationState
    data class Success(val category: Category) : CategoryCreationState
    data class Error(val message: String) : CategoryCreationState
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepo: BudgetRepository,
    private val categoryRepo: CategoryRepository
) : ViewModel() {

    companion object {
        private const val HARDCODED_USER_ID = "6904cf1320173db06b2641b8"
    }

    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Idle)
    val uiState: StateFlow<BudgetUiState> = _uiState

    private val _categoryCreationState = MutableStateFlow<CategoryCreationState>(CategoryCreationState.Idle)
    val categoryCreationState: StateFlow<CategoryCreationState> = _categoryCreationState

    init {
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = BudgetUiState.Loading
            
            // Fetch both budgets and categories
            val budgetsResult = budgetRepo.getBudgets()
            val categoriesResult = budgetRepo.getCategories()
            
            if (budgetsResult.isSuccess && categoriesResult.isSuccess) {
                val allBudgets = budgetsResult.getOrThrow()
                val allCategories = categoriesResult.getOrThrow()
                
                // Filter only expense categories that belong to current user
                val expenseCategories = allCategories.filter { 
                    it.type.equals("Expense", ignoreCase = true) && 
                    it.userId == HARDCODED_USER_ID 
                }
                
                _uiState.value = BudgetUiState.Success(
                    budgets = allBudgets,
                    categories = expenseCategories
                )
            } else {
                val error = budgetsResult.exceptionOrNull() ?: categoriesResult.exceptionOrNull()
                _uiState.value = BudgetUiState.Error(error?.message ?: "Unknown error")
            }
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            val result = budgetRepo.deleteBudget(id)
            if (result.isSuccess) {
                loadBudgets() // Reload after delete
            } else {
                _uiState.value = BudgetUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to delete budget"
                )
            }
        }
    }

    fun updateBudget(budgetId: String, newLimitAmount: Double) {
        viewModelScope.launch {
            // Get current budget to preserve other fields
            val currentState = _uiState.value
            if (currentState is BudgetUiState.Success) {
                val budget = currentState.budgets.find { it.id == budgetId }
                if (budget != null) {
                    val request = com.example.financemanagement.data.remote.models.BudgetRequest(
                        categoryId = budget.categoryId,
                        limitAmount = newLimitAmount,
                        month = budget.month,
                        year = budget.year
                    )
                    
                    val result = budgetRepo.updateBudget(budgetId, request)
                    if (result.isSuccess) {
                        loadBudgets() // Reload after update
                    } else {
                        _uiState.value = BudgetUiState.Error(
                            result.exceptionOrNull()?.message ?: "Failed to update budget"
                        )
                    }
                } else {
                    _uiState.value = BudgetUiState.Error("Budget not found")
                }
            }
        }
    }

    /**
     * Create or update budget for a category
     * If budgetId is null, create new budget
     * If budgetId exists, update existing budget
     */
    fun createOrUpdateBudget(
        budgetId: String?,
        categoryId: String,
        newLimitAmount: Double,
        month: Int,
        year: Int
    ) {
        viewModelScope.launch {
            val request = com.example.financemanagement.data.remote.models.BudgetRequest(
                categoryId = categoryId,
                limitAmount = newLimitAmount,
                month = month,
                year = year
            )
            
            val result = if (budgetId == null) {
                // Create new budget
                budgetRepo.createBudget(request)
            } else {
                // Update existing budget
                budgetRepo.updateBudget(budgetId, request)
            }
            
            if (result.isSuccess) {
                loadBudgets() // Reload after create/update
            } else {
                _uiState.value = BudgetUiState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to save budget"
                )
            }
        }
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            _categoryCreationState.value = CategoryCreationState.Creating
            
            // Categories for budget are Expense type only
            val request = CategoryRequest(
                name = name,
                type = "Expense"
            )
            
            val result = categoryRepo.createCategory(request)
            
            if (result.isSuccess) {
                val category = result.getOrThrow()
                _categoryCreationState.value = CategoryCreationState.Success(category)
                // Reload budgets to get updated categories list
                loadBudgets()
            } else {
                _categoryCreationState.value = CategoryCreationState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to create category"
                )
            }
        }
    }

    fun resetCategoryCreationState() {
        _categoryCreationState.value = CategoryCreationState.Idle
    }
}
