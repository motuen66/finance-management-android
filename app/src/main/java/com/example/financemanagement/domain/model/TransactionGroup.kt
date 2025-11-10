package com.example.financemanagement.domain.model

data class TransactionGroup(
    val dateHeader: String, // e.g., "Today", "07 November, Friday"
    val transactions: List<Transaction>
)
