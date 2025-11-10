package com.example.financemanagement.domain.model

data class TransactionSummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val balance: Double,
    val transactions: List<Transaction> = emptyList()
)
