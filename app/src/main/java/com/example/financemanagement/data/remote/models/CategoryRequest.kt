package com.example.financemanagement.data.remote.models

data class CategoryRequest(
    val name: String,
    val type: String, // "Income" or "Expense"
    val userId: String = "" // Will be handled by backend from JWT token, but required by API
)
