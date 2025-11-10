package com.example.financemanagement.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.R
import com.example.financemanagement.data.repository.TransactionRepository
import com.example.financemanagement.data.repository.SavingGoalRepository
import com.example.financemanagement.data.repository.BudgetRepository
import com.example.financemanagement.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class SpendingCategory(
    val name: String,
    val amount: Long,
    val percent: Float,
    val color: Int,
    val iconRes: Int
)

data class SpendingReportState(
    val totalAmount: Long = 0,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val totalSavings: Long = 0,  // Total from saving goals
    val totalBudget: Long = 0,   // Total from budgets
    val period: String = "Month", // "Week", "Month", "Year"
    val categories: List<SpendingCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val savingGoalRepository: SavingGoalRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _reportState = MutableStateFlow(SpendingReportState())
    val reportState: StateFlow<SpendingReportState> = _reportState.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()
    
    // Month navigation
    private val calendar = Calendar.getInstance()
    var currentMonth: Int = calendar.get(Calendar.MONTH) + 1 // 1-12
        private set
    var currentYear: Int = calendar.get(Calendar.YEAR)
        private set
    
    // Date parser for backend format: yyyy-MM-dd'T'HH:mm:ss
    private val dateParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
    
    // Predefined color palette for categories
    private val categoryColorPalette = listOf(
        android.graphics.Color.parseColor("#FFA26B"), // Orange
        android.graphics.Color.parseColor("#6BCF7F"), // Green
        android.graphics.Color.parseColor("#9D7BEA"), // Purple
        android.graphics.Color.parseColor("#FF6B9D"), // Pink/Red
        android.graphics.Color.parseColor("#5B8DEE"), // Blue
        android.graphics.Color.parseColor("#4ECDC4"), // Teal
        android.graphics.Color.parseColor("#FFD93D"), // Yellow
        android.graphics.Color.parseColor("#FF6B6B")  // Red
    )

    init {
        fetchTransactions()
        fetchSavingGoals()
        fetchBudgets()
    }

    fun previousMonth() {
        if (currentMonth == 1) {
            currentMonth = 12
            currentYear--
        } else {
            currentMonth--
        }
        Log.d("ReportsViewModel", "previousMonth: $currentMonth/$currentYear")
        // Recalculate with new month/year filter
        calculateSpending()
    }

    fun nextMonth() {
        if (currentMonth == 12) {
            currentMonth = 1
            currentYear++
        } else {
            currentMonth++
        }
        Log.d("ReportsViewModel", "nextMonth: $currentMonth/$currentYear")
        // Recalculate with new month/year filter
        calculateSpending()
    }

    fun setPeriod(period: String) {
        _reportState.value = _reportState.value.copy(period = period)
        calculateSpending()
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            _reportState.value = _reportState.value.copy(isLoading = true)
            
            transactionRepository.getTransactions().fold(
                onSuccess = { transactions ->
                    allTransactions = transactions
                    calculateSpending()
                    _reportState.value = _reportState.value.copy(
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { error ->
                    _reportState.value = _reportState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }

    private fun fetchSavingGoals() {
        viewModelScope.launch {
            savingGoalRepository.getSavingGoals().fold(
                onSuccess = { goals ->
                    // Calculate total current amount from all saving goals
                    val totalSavings = goals.sumOf { it.currentAmount }.toLong()
                    _reportState.value = _reportState.value.copy(totalSavings = totalSavings)
                    Log.d("ReportsViewModel", "Total Savings from goals: $totalSavings")
                },
                onFailure = { error ->
                    Log.e("ReportsViewModel", "Failed to fetch saving goals: ${error.message}")
                }
            )
        }
    }

    private fun fetchBudgets() {
        viewModelScope.launch {
            budgetRepository.getBudgets().fold(
                onSuccess = { budgets ->
                    // Calculate total budget amount from limitAmount field
                    val totalBudget = budgets.sumOf { it.limitAmount }.toLong()
                    _reportState.value = _reportState.value.copy(totalBudget = totalBudget)
                    Log.d("ReportsViewModel", "Total Budget: $totalBudget")
                },
                onFailure = { error ->
                    Log.e("ReportsViewModel", "Failed to fetch budgets: ${error.message}")
                }
            )
        }
    }

    private fun calculateSpending() {
        val period = _reportState.value.period
        val filteredTransactions = filterTransactionsByPeriod(allTransactions, period)
        
        Log.d("ReportsViewModel", "calculateSpending for $currentMonth/$currentYear: ${filteredTransactions.size} transactions")
        
        // Calculate total income and expense
        val incomeTransactions = filteredTransactions.filter { it.type.equals("INCOME", ignoreCase = true) }
        val expenseTransactions = filteredTransactions.filter { it.type.equals("EXPENSE", ignoreCase = true) }
        
        val totalIncome = incomeTransactions.sumOf { it.amount }.toLong()
        val totalExpense = expenseTransactions.sumOf { it.amount }.toLong()
        
        Log.d("ReportsViewModel", "Income: $totalIncome, Expense: $totalExpense")
        
        // Handle case when there are no expenses (but might have income)
        if (expenseTransactions.isEmpty()) {
            _reportState.value = _reportState.value.copy(
                totalAmount = 0,
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                categories = emptyList(),
                isLoading = false,
                error = null
            )
            return
        }

        // Group by category
        val categoryMap = mutableMapOf<String, Double>()
        expenseTransactions.forEach { transaction ->
            val categoryName = transaction.categoryName?.takeIf { it.isNotBlank() }
                ?: transaction.category?.name
                ?: "Other"
            
            categoryMap[categoryName] = categoryMap.getOrDefault(categoryName, 0.0) + transaction.amount
        }

        // Calculate total
        val total = categoryMap.values.sum()

        // Convert to SpendingCategory list with colors and icons
        val categories = categoryMap.entries
            .sortedByDescending { it.value }
            .mapIndexed { index, entry ->
                SpendingCategory(
                    name = entry.key,
                    amount = entry.value.toLong(),
                    percent = ((entry.value / total) * 100).toFloat(),
                    color = getCategoryColor(index),
                    iconRes = getCategoryIcon(entry.key)
                )
            }

        _reportState.value = _reportState.value.copy(
            totalAmount = total.toLong(),
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            categories = categories,
            isLoading = false,
            error = null
        )
    }

    private fun filterTransactionsByPeriod(transactions: List<Transaction>, period: String): List<Transaction> {
        return transactions.filter { transaction ->
            try {
                // Parse transaction date (format: yyyy-MM-dd'T'HH:mm:ss)
                val transactionDate = dateParser.parse(transaction.date) ?: return@filter false
                
                val transactionCalendar = Calendar.getInstance().apply {
                    time = transactionDate
                }
                
                // Filter by current month and year
                val transactionMonth = transactionCalendar.get(Calendar.MONTH) + 1 // 1-12
                val transactionYear = transactionCalendar.get(Calendar.YEAR)
                
                transactionMonth == currentMonth && transactionYear == currentYear
            } catch (e: Exception) {
                // If date parsing fails, exclude this transaction
                false
            }
        }
    }

    private fun getCategoryColor(index: Int): Int {
        // Use predefined color palette, cycle through if there are more categories than colors
        return categoryColorPalette[index % categoryColorPalette.size]
    }

    private fun getCategoryIcon(categoryName: String): Int {
        // Use default icon for now. Add specific icons in res/drawable as needed.
        return R.drawable.ic_category_default
    }
}
