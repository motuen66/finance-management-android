package com.example.financemanagement.data.repository

import com.example.financemanagement.data.local.TokenManager
import com.example.financemanagement.data.remote.ApiService
import com.example.financemanagement.data.remote.models.LoginRequest
import com.example.financemanagement.data.remote.models.LoginResponse
import com.example.financemanagement.data.remote.models.RegisterRequest
import com.example.financemanagement.data.remote.models.RegisterResponse
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = api.login(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Persist token
                    tokenManager.saveToken(body.token)
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                // Parse error from server
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Result.failure(Exception("Login failed: $errorMessage"))
            }
        } catch (e: IOException) {
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val response = api.register(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    tokenManager.saveToken(body.token)
                    Result.success(body)
                } else {
                    android.util.Log.e("AuthRepo", "Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    android.util.Log.e("AuthRepo", "Error parsing error body", e)
                    response.message()
                }
                android.util.Log.e("AuthRepo", "Registration failed: $errorMessage")
                Result.failure(Exception("Registration failed: $errorMessage"))
            }
        } catch (e: IOException) {
            android.util.Log.e("AuthRepo", "Network error during register", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            android.util.Log.e("AuthRepo", "Unknown error during register", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun logout() {
        tokenManager.clearToken()
    }

    override fun observeToken(): Flow<String?> = tokenManager.tokenFlow
}
