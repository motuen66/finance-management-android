package com.example.financemanagement.domain.model

import com.google.gson.annotations.SerializedName

data class Transaction(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("note")
    val note: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("type")
    val type: String, // "Income" or "Expense"
    
    @SerializedName("categoryId")
    val categoryId: String,
    
    val category: Category? = null
)
