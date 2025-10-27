package com.example.financemanagement.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val passwordHash: String? = null, // Không cần thiết cho mobile, chỉ backend
    val createdAt: String? = null,
    val updatedAt: String? = null
)
