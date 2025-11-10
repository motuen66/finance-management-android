package com.example.financemanagement.data.remote

import com.example.financemanagement.data.remote.models.LoginRequest
import com.example.financemanagement.data.remote.models.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // TODO: thêm các endpoint khác: transactions, categories, budgets, ...
}
