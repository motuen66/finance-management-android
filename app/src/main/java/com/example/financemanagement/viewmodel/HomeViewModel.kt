package com.example.financemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.repository.TransactionRepository
import com.example.financemanagement.domain.model.Transaction
import com.example.financemanagement.domain.model.TransactionGroup
import com.example.financemanagement.domain.model.TransactionSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _transactionGroups = MutableStateFlow<List<TransactionGroup>>(emptyList())
    val transactionGroups: StateFlow<List<TransactionGroup>> = _transactionGroups

    private val _summary = MutableStateFlow(TransactionSummary(0.0, 0.0, 0.0))
    val summary: StateFlow<TransactionSummary> = _summary

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var allTransactions = listOf<Transaction>()
    private var currentDaysToShow = 7

    init {
        fetchTransactions()
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = transactionRepository.getTransactions()

            if (result.isSuccess) {
                allTransactions = result.getOrThrow()
                android.util.Log.d("HomeViewModel", "Fetched ${allTransactions.size} transactions")
                
                // Calculate summary
                calculateSummary(allTransactions)
                
                // Group by date and show initial 7 days
                updateDisplayedTransactions()
            } else {
                val error = result.exceptionOrNull()?.message ?: "Unknown error"
                android.util.Log.e("HomeViewModel", "Error fetching transactions: $error")
                _error.value = error
            }

            _isLoading.value = false
        }
    }

    fun loadMoreTransactions() {
        currentDaysToShow += 7
        updateDisplayedTransactions()
    }

    private fun updateDisplayedTransactions() {
        // Filter transactions from last N days
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val endDate = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, -currentDaysToShow)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startDate = calendar.time

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        
        val filteredTransactions = allTransactions.filter { transaction ->
            try {
                val transactionDate = dateFormat.parse(transaction.date)
                transactionDate != null && transactionDate.after(startDate) && transactionDate.before(endDate)
            } catch (e: Exception) {
                false
            }
        }

        // Group by date
        val grouped = groupTransactionsByDate(filteredTransactions)
        _transactionGroups.value = grouped
    }

    private fun groupTransactionsByDate(transactions: List<Transaction>): List<TransactionGroup> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        // Group transactions by date string (YYYY-MM-DD)
        val grouped = transactions
            .sortedByDescending { it.date }
            .groupBy { transaction ->
                try {
                    val date = dateFormat.parse(transaction.date)
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date ?: Date())
                } catch (e: Exception) {
                    "Unknown"
                }
            }

        return grouped.map { (dateStr, transactions) ->
            val headerText = try {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dateStr)
                val cal = Calendar.getInstance().apply { time = date ?: Date() }

                when {
                    isSameDay(cal, today) -> "Today"
                    isSameDay(cal, yesterday) -> "Yesterday"
                    else -> {
                        // Format: "07 November, Friday"
                        SimpleDateFormat("dd MMMM, EEEE", Locale.US).format(date ?: Date())
                    }
                }
            } catch (e: Exception) {
                dateStr
            }

            TransactionGroup(headerText, transactions)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun calculateSummary(transactions: List<Transaction>) {
        var totalIncome = 0.0
        var totalExpense = 0.0

        transactions.forEach { transaction ->
            if (transaction.type == "Income") {
                totalIncome += transaction.amount
            } else if (transaction.type == "Expense") {
                totalExpense += transaction.amount
            }
        }

        val balance = totalIncome - totalExpense
        _summary.value = TransactionSummary(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            transactions = transactions
        )
    }

    fun refreshTransactions() {
        currentDaysToShow = 7
        fetchTransactions()
    }
}