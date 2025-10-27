package com.example.financemanagement.domain.model

import com.google.gson.annotations.SerializedName

data class SavingGoal(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String? = null,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("goalAmount")
    val goalAmount: Double,
    
    @SerializedName("currentAmount")
    val currentAmount: Double,
    
    @SerializedName("goalDate")
    val goalDate: String,
    
    @SerializedName("isCompleted")
    val isCompleted: Boolean
)
