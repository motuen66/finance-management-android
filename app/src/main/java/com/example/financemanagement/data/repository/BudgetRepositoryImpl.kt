package com.example.financemanagement.data.repository

import android.util.Log
import com.example.financemanagement.data.local.dao.BudgetDao
import com.example.financemanagement.data.local.dao.CategoryDao
import com.example.financemanagement.data.local.entities.toDomainModel
import com.example.financemanagement.data.local.entities.toEntity
import com.example.financemanagement.data.remote.models.BudgetRequest
import com.example.financemanagement.domain.model.Category
import com.example.financemanagement.data.remote.ApiService
import com.example.financemanagement.domain.model.Budget
import java.io.IOException
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao
) : BudgetRepository {

    companion object {
        private const val HARDCODED_USER_ID = "6904cf1320173db06b2641b8"
    }

    override suspend fun getBudgets(): Result<List<Budget>> {
        return try {
            val response = api.getBudgets()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Save to local database for offline access
                    try {
                        val entities = body.map { it.toEntity() }
                        budgetDao.insertBudgets(entities)
                    } catch (e: Exception) {
                        Log.e("BudgetRepo", "Failed to cache budgets locally", e)
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("BudgetRepo", "Failed to fetch budgets: $errorMessage")
                Result.failure(Exception("Failed to fetch budgets: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("BudgetRepo", "Network error", e)
            // Try to get from local database if network fails
            try {
                val localBudgets = budgetDao.getAllBudgets()
                if (localBudgets.isNotEmpty()) {
                    Log.d("BudgetRepo", "Using cached budgets from local database")
                    return Result.success(localBudgets.map { it.toDomainModel() })
                }
            } catch (dbError: Exception) {
                Log.e("BudgetRepo", "Failed to get budgets from local database", dbError)
            }
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun getBudgetById(id: String): Result<Budget> {
        return try {
            val response = api.getBudgetById(id)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("BudgetRepo", "Failed to fetch budget: $errorMessage")
                Result.failure(Exception("Failed to fetch budget: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("BudgetRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun createBudget(request: BudgetRequest): Result<Budget> {
        return try {
            // Create request with hardcoded userId
            val requestWithUserId = request.copy(userId = HARDCODED_USER_ID)
            
            val response = api.createBudget(requestWithUserId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Save to local database
                    try {
                        budgetDao.insertBudget(body.toEntity())
                        Log.d("BudgetRepo", "Budget saved to local database")
                    } catch (e: Exception) {
                        Log.e("BudgetRepo", "Failed to save budget to local database", e)
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("BudgetRepo", "Failed to create budget: $errorMessage")
                Result.failure(Exception("Failed to create budget: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("BudgetRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun updateBudget(id: String, request: BudgetRequest): Result<Budget> {
        return try {
            // Create request with hardcoded userId
            val requestWithUserId = request.copy(userId = HARDCODED_USER_ID)
            
            val response = api.updateBudget(id, requestWithUserId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Update local database
                    try {
                        budgetDao.insertBudget(body.toEntity()) // Insert or replace
                        Log.d("BudgetRepo", "Budget updated in local database")
                    } catch (e: Exception) {
                        Log.e("BudgetRepo", "Failed to update budget in local database", e)
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("BudgetRepo", "Failed to update budget: $errorMessage")
                Result.failure(Exception("Failed to update budget: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("BudgetRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun deleteBudget(id: String): Result<Unit> {
        return try {
            val response = api.deleteBudget(id)
            
            if (response.isSuccessful) {
                // Delete from local database
                try {
                    budgetDao.deleteBudgetById(id)
                    Log.d("BudgetRepo", "Budget deleted from local database")
                } catch (e: Exception) {
                    Log.e("BudgetRepo", "Failed to delete budget from local database", e)
                }
                Result.success(Unit)
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("BudgetRepo", "Failed to delete budget: $errorMessage")
                Result.failure(Exception("Failed to delete budget: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("BudgetRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = api.getCategories()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Save to local database for offline access
                    try {
                        val entities = body.map { it.toEntity() }
                        categoryDao.insertCategories(entities)
                    } catch (e: Exception) {
                        Log.e("BudgetRepo", "Failed to cache categories locally", e)
                    }
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("BudgetRepo", "Failed to fetch categories: $errorMessage")
                Result.failure(Exception("Failed to fetch categories: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("BudgetRepo", "Network error", e)
            // Try to get from local database if network fails
            try {
                val localCategories = categoryDao.getAllCategories()
                if (localCategories.isNotEmpty()) {
                    Log.d("BudgetRepo", "Using cached categories from local database")
                    return Result.success(localCategories.map { it.toDomainModel() })
                }
            } catch (dbError: Exception) {
                Log.e("BudgetRepo", "Failed to get categories from local database", dbError)
            }
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("BudgetRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

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