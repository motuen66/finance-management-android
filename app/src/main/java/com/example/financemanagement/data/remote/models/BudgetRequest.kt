package com.example.financemanagement.data.remote.models

data class BudgetRequest(
    val categoryId: String,
    val limitAmount: Double,
    val month: Int,
    val year: Int,
    val userId: String = "" // Required by API, will be filled from JWT token
)
