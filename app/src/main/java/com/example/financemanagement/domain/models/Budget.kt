package com.example.financemanagement.domain.model

import com.google.gson.annotations.SerializedName

data class Budget(
    @SerializedName("id")
    val id: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("categoryId")
    val categoryId: String,

    @SerializedName("limitAmount")
    val limitAmount: Double,

    @SerializedName("month")
    val month: Int,

    @SerializedName("year")
    val year: Int,

    @SerializedName("categoryName")
    val categoryName: String,
)
