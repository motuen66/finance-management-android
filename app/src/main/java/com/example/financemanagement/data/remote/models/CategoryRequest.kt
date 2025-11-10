package com.example.financemanagement.data.remote.models

data class CategoryRequest(
    val name: String,
    val type: String, // "Income" or "Expense"
<<<<<<< Updated upstream
    val userId: String = "" // Will be handled by backend from JWT token, but required by API
=======
    val userId: String? = null // Optional - backend can extract from JWT if needed
>>>>>>> Stashed changes
)
