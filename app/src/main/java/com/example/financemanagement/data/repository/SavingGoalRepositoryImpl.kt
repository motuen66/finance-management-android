package com.example.financemanagement.data.repository

import android.util.Log
import com.example.financemanagement.data.remote.ApiService
import com.example.financemanagement.domain.model.SavingGoal
import java.io.IOException
import javax.inject.Inject

class SavingGoalRepositoryImpl @Inject constructor(
    private val api: ApiService
) : SavingGoalRepository {

    override suspend fun getSavingGoals(): Result<List<SavingGoal>> {
        return try {
            val response = api.getSavingGoals()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: "Server error ${response.code()}: ${response.message()}"
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("SavingGoalRepo", "Failed to fetch saving goals: $errorMessage")
                Result.failure(Exception("Failed to fetch saving goals: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("SavingGoalRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }
}
