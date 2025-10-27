package com.example.financemanagement.domain.model

data class Budget(
    val id: String,
    val userId: String,
    val categoryId: String,
    val limitAmount: Double,
    val month: Int,
    val year: Int
)
