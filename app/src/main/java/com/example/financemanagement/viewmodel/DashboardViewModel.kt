package com.example.financemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.repository.SavingGoalRepository
import com.example.financemanagement.data.repository.TransactionRepository
import com.example.financemanagement.domain.model.SavingGoal
import com.example.financemanagement.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val savingGoalRepository: SavingGoalRepository
): ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    private val _savingGoals = MutableStateFlow<List<SavingGoal>>(emptyList())
    val savingGoals: StateFlow<List<SavingGoal>> = _savingGoals

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = transactionRepository.getTransactions()
            
            if (result.isSuccess) {
                _transactions.value = result.getOrThrow()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
            
            _isLoading.value = false
        }
    }

    fun fetchSavingGoals() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = savingGoalRepository.getSavingGoals()
            
            if (result.isSuccess) {
                _savingGoals.value = result.getOrThrow()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
            }
            
            _isLoading.value = false
        }
    }
}