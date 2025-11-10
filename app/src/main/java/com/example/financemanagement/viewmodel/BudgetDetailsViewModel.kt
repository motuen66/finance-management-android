package com.example.financemanagement.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.repository.BudgetRepository
import com.example.financemanagement.data.repository.TransactionRepository
import com.example.financemanagement.domain.model.Budget
import com.example.financemanagement.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

sealed interface BudgetDetailsUiState {
    object Loading : BudgetDetailsUiState
    data class Success(
        val budget: Budget,
        val spentAmount: Double,
        val transactions: List<Transaction>,
        val percentage: Int
    ) : BudgetDetailsUiState
    data class Error(val message: String) : BudgetDetailsUiState
}

@HiltViewModel
class BudgetDetailsViewModel @Inject constructor(
    application: Application,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<BudgetDetailsUiState>(BudgetDetailsUiState.Loading)
    val uiState: StateFlow<BudgetDetailsUiState> = _uiState.asStateFlow()

    fun loadBudgetDetails(budgetId: String) {
        viewModelScope.launch {
            _uiState.value = BudgetDetailsUiState.Loading
            
            try {
                Log.d("BudgetDetailsVM", "Loading budget details for ID: $budgetId")
                
                // Get budget by ID
                val budgetResult = budgetRepository.getBudgetById(budgetId)
                
                if (budgetResult.isFailure) {
                    val error = budgetResult.exceptionOrNull()?.message ?: "Failed to load budget"
                    Log.e("BudgetDetailsVM", "Error loading budget: $error")
                    _uiState.value = BudgetDetailsUiState.Error(error)
                    return@launch
                }
                
                val budget = budgetResult.getOrThrow()
                Log.d("BudgetDetailsVM", "Budget loaded: ${budget.categoryName}")
                
                // Get all transactions
                val transactionsResult = transactionRepository.getTransactions()
                
                if (transactionsResult.isFailure) {
                    val error = transactionsResult.exceptionOrNull()?.message ?: "Failed to load transactions"
                    Log.e("BudgetDetailsVM", "Error loading transactions: $error")
                    _uiState.value = BudgetDetailsUiState.Error(error)
                    return@launch
                }
                
                val allTransactions = transactionsResult.getOrThrow()
                
                // Filter transactions for current month and this budget's category
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH) + 1
                val currentYear = calendar.get(Calendar.YEAR)
                
                val filteredTransactions = allTransactions.filter { transaction ->
                    // Only expense transactions
                    if (!transaction.type.equals("expense", ignoreCase = true)) {
                        return@filter false
                    }
                    
                    // Match category
                    if (transaction.categoryId != budget.categoryId) {
                        return@filter false
                    }
                    
                    // Match current month and year
                    try {
                        val parts = transaction.date.split("-")
                        if (parts.size >= 3) {
                            val year = parts[0].toInt()
                            val month = parts[1].toInt()
                            month == currentMonth && year == currentYear
                        } else {
                            false
                        }
                    } catch (e: Exception) {
                        Log.e("BudgetDetailsVM", "Error parsing transaction date: ${transaction.date}", e)
                        false
                    }
                }
                
                // Calculate spent amount
                val spentAmount = filteredTransactions.sumOf { it.amount }
                
                // Calculate percentage
                val percentage = if (budget.limitAmount > 0) {
                    ((spentAmount / budget.limitAmount) * 100).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                
                Log.d("BudgetDetailsVM", "Spent: $spentAmount / ${budget.limitAmount} ($percentage%)")
                Log.d("BudgetDetailsVM", "Filtered ${filteredTransactions.size} transactions")
                
                _uiState.value = BudgetDetailsUiState.Success(
                    budget = budget,
                    spentAmount = spentAmount,
                    transactions = filteredTransactions,
                    percentage = percentage
                )
                
            } catch (e: Exception) {
                Log.e("BudgetDetailsVM", "Error loading budget details", e)
                _uiState.value = BudgetDetailsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
