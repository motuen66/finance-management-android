package com.example.financemanagement.data.remote.models

import com.google.gson.annotations.SerializedName

// Register endpoint now returns same format as Login (via JwtService.Authenticate)
data class RegisterResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("expiresIn")
    val expiresIn: Int
)
