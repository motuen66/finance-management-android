package com.example.financemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.local.TokenManager
import com.example.financemanagement.data.remote.models.TransactionRequest
import com.example.financemanagement.data.repository.BudgetRepository
import com.example.financemanagement.data.repository.TransactionRepository
import com.example.financemanagement.domain.model.Budget
import com.example.financemanagement.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeAddExpenseViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _transactionCreated = MutableStateFlow<Transaction?>(null)
    val transactionCreated: StateFlow<Transaction?> = _transactionCreated

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId

    fun setSelectedCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun fetchBudgets() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val userId = tokenManager.getUserId()
            if (userId == null) {
                android.util.Log.e("HomeAddExpenseViewModel", "Cannot fetch budgets: no userId")
                _error.value = "User not authenticated"
                _isLoading.value = false
                return@launch
            }

            android.util.Log.d("HomeAddExpenseViewModel", "Fetching budgets for userId: $userId")
            val result = budgetRepository.getBudgetsByUserId(userId)

            if (result.isSuccess) {
                val budgets = result.getOrThrow()
                android.util.Log.d("HomeAddExpenseViewModel", "Fetched ${budgets.size} budgets: $budgets")
                _budgets.value = budgets
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                android.util.Log.e("HomeAddExpenseViewModel", "Error fetching budgets: $error")
                _error.value = error
            }

            _isLoading.value = false
        }
    }

    fun createTransaction(
        note: String,
        amount: Double,
        date: String,
        type: String = "Expense",
        categoryId: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _transactionCreated.value = null

            val userId = tokenManager.getUserId()
            if (userId == null) {
                android.util.Log.e("HomeAddExpenseViewModel", "Cannot create transaction: no userId")
                _error.value = "User not authenticated"
                _isLoading.value = false
                return@launch
            }

            android.util.Log.d(
                "HomeAddExpenseViewModel",
                "Creating transaction: note=$note, amount=$amount, date=$date, type=$type, categoryId=$categoryId, userId=$userId"
            )

            val request = TransactionRequest(
                note = note,
                amount = amount,
                date = date,
                userId = userId,
                type = type,
                categoryId = categoryId
            )

            val result = transactionRepository.createTransaction(request)

            if (result.isSuccess) {
                val transaction = result.getOrThrow()
                android.util.Log.d("HomeAddExpenseViewModel", "Transaction created: ${transaction.id}")
                _transactionCreated.value = transaction
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                android.util.Log.e("HomeAddExpenseViewModel", "Error creating transaction: $error")
                _error.value = error
            }

            _isLoading.value = false
        }
    }
}