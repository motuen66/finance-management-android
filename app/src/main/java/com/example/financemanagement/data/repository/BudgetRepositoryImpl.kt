package com.example.financemanagement.data.repository

import android.util.Log
import com.example.financemanagement.data.remote.ApiService
import com.example.financemanagement.domain.model.Budget
import java.io.IOException
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val api: ApiService
) : BudgetRepository {

    override suspend fun getBudgetsByUserId(userId: String): Result<List<Budget>> {
        return try {
            android.util.Log.d("BudgetRepositoryImpl", "Calling API for userId: $userId")
            val response = api.getBudgets()
            try {
                android.util.Log.d(
                    "BudgetRepositoryImpl",
                    "Received response: code=${response.code()}, message=${response.message()}"
                )
            } catch (e: Exception) {
                android.util.Log.e("BudgetRepositoryImpl", "Failed to log response metadata", e)
            }

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Debug: log each budget userId before filtering
                    android.util.Log.d("BudgetRepositoryImpl", "API success, total budgets: ${body.size}")
                    body.forEachIndexed { index, b ->
                        android.util.Log.d(
                            "BudgetRepositoryImpl",
                            "item[$index]: id=${b.id}, userId=${b.userId}, categoryId=${b.categoryId}, limit=${b.limitAmount}"
                        )
                    }
                    // filter by userId on client side because backend currently returns all budgets
                    val filtered = body.filter { it.userId == userId }
                    android.util.Log.d("BudgetRepositoryImpl", "Filtered budgets count: ${filtered.size}")
                    Result.success(filtered)
                } else {
                    android.util.Log.e("BudgetRepositoryImpl", "Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                android.util.Log.e("BudgetRepositoryImpl", "API failed: $errorMessage")
                Result.failure(Exception("Failed to fetch budgets: $errorMessage"))
            }
        } catch (e: IOException) {
            android.util.Log.e("BudgetRepositoryImpl", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            android.util.Log.e("BudgetRepositoryImpl", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }
}