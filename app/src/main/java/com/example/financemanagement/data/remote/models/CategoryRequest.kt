package com.example.financemanagement.data.remote.models

data class CategoryRequest(
    val name: String,
    val type: String, // "Income" or "Expense"
    val userId: String? = null // Optional - backend can extract from JWT if needed
)
