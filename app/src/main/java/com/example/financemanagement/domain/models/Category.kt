package com.example.financemanagement.domain.model

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("type")
    val type: String, // "Income" or "Expense"
    
    @SerializedName("userId")
    val userId: String
)
