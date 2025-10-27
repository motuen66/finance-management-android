package com.example.financemanagement.domain.model

data class Report(
    val id: String,
    val userId: String,
    val month: Int,
    val year: Int,
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val categoryBreakdown: List<CategoryBreakdown>
)

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val type: String // "income" or "expense"
)
