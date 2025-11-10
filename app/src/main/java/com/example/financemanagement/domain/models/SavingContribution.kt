package com.example.financemanagement.domain.model

import com.google.gson.annotations.SerializedName

data class SavingContribution(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("goalId")
    val goalId: String,
    
    @SerializedName("amount")
    val amount: Double,
    
    @SerializedName("note")
    val note: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String // ISO format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
)