package com.example.financemanagement.data.remote.models

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("expiresIn")
    val expiresIn: Int
)
