package com.example.financemanagement.data.repository

import com.example.financemanagement.data.remote.models.LoginRequest
import com.example.financemanagement.data.remote.models.LoginResponse
import com.example.financemanagement.data.remote.models.RegisterRequest
import com.example.financemanagement.data.remote.models.RegisterResponse
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
    suspend fun logout()
    fun observeToken(): Flow<String?>
}
