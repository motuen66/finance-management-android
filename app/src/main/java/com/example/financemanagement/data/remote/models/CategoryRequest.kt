package com.example.financemanagement.data.remote.models

data class CategoryRequest(
    val name: String,
    val type: String // "Income" or "Expense"
)
