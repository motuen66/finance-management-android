package com.example.financemanagement.data.remote.models

data class SavingGoalRequest(
    val title: String,
    val goalAmount: Double,
    val currentAmount: Double,
    val goalDate: String
)
