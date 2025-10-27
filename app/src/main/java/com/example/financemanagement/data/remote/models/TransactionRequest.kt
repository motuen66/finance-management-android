package com.example.financemanagement.data.remote.models

data class TransactionRequest(
    val note: String,
    val amount: Double,
    val date: String,
    val type: String, // "Income" or "Expense"
    val categoryId: String
)
