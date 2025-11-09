package com.example.financemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.repository.TransactionRepository
import com.example.financemanagement.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardSummary(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val balance: Double = 0.0,
    val transactions: List<Transaction> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _summary = MutableStateFlow(DashboardSummary())
    val summary: StateFlow<DashboardSummary> = _summary

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchTransactions()
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = transactionRepository.getTransactions()

            if (result.isSuccess) {
                val transactions = result.getOrThrow()
                calculateSummary(transactions)
            } else {
                _error.value = result.exceptionOrNull()?.message
            }

            _isLoading.value = false
        }
    }

    private fun calculateSummary(transactions: List<Transaction>) {
        val income = transactions
            .filter { it.type.equals("INCOME", ignoreCase = true) }
            .sumOf { it.amount }

        val expense = transactions
            .filter { it.type.equals("EXPENSE", ignoreCase = true) }
            .sumOf { it.amount }

        val balance = income - expense

        _summary.value = DashboardSummary(
            income = income,
            expense = expense,
            balance = balance,
            transactions = transactions
        )
    }

    fun refreshData() {
        fetchTransactions()
    }
}
