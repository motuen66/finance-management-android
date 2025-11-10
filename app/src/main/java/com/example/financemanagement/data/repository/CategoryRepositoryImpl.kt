package com.example.financemanagement.data.repository

import android.util.Log
import com.example.financemanagement.data.local.TokenManager
import com.example.financemanagement.data.local.dao.CategoryDao
import com.example.financemanagement.data.local.entities.toDomainModel
import com.example.financemanagement.data.local.entities.toEntity
import com.example.financemanagement.data.remote.api.ApiService
import com.example.financemanagement.data.remote.models.CategoryRequest
import com.example.financemanagement.domain.model.Category
import java.io.IOException
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val api: ApiService,

    private val categoryDao: CategoryDao,

    private val tokenManager: TokenManager
) : CategoryRepository {

    override suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = api.getCategories()
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
                Log.e("CategoryRepo", "Failed to fetch categories: $errorMessage")
                Result.failure(Exception("Failed to fetch categories: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("CategoryRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun getCategoryById(id: String): Result<Category> {
        return try {
            val response = api.getCategoryById(id)
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
                Log.e("CategoryRepo", "Failed to fetch category: $errorMessage")
                Result.failure(Exception("Failed to fetch category: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("CategoryRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun createCategory(name: String, type: String): Result<Category> {
        return try {
            val userId = tokenManager.getUserIdFromToken()
            val request = CategoryRequest(name = name, type = type, userId = userId)
            val response = api.createCategory(request)
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
                Log.e("CategoryRepo", "Failed to create category: $errorMessage")
                Result.failure(Exception("Failed to create category: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("CategoryRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun updateCategory(id: String, name: String, type: String): Result<Category> {
        return try {
            val userId = tokenManager.getUserIdFromToken()
            val request = CategoryRequest(name = name, type = type, userId = userId)
            val response = api.updateCategory(id, request)
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
                Log.e("CategoryRepo", "Failed to update category: $errorMessage")
                Result.failure(Exception("Failed to update category: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("CategoryRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun deleteCategory(id: String): Result<Unit> {
        return try {
            val response = api.deleteCategory(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("CategoryRepo", "Failed to delete category: $errorMessage")
                Result.failure(Exception("Failed to delete category: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("CategoryRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun createCategory(request: CategoryRequest): Result<Category> {
        return try {
            // Extract userId from token
            val userId = tokenManager.getUserId()
            if (userId == null) {
                Log.e("CategoryRepo", "User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Create request with userId from token
            val requestWithUserId = request.copy(userId = userId)
            
            val response = api.createCategory(requestWithUserId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Save to local database
                    try {
                        categoryDao.insertCategory(body.toEntity())
                        Log.d("CategoryRepo", "Category saved to local database")
                    } catch (e: Exception) {
                        Log.e("CategoryRepo", "Failed to save category to local database", e)
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
                Log.e("CategoryRepo", "Failed to create category: $errorMessage")
                Result.failure(Exception("Failed to create category: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("CategoryRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun updateCategory(id: String, request: CategoryRequest): Result<Category> {
        return try {
            // Extract userId from token
            val userId = tokenManager.getUserId()
            if (userId == null) {
                Log.e("CategoryRepo", "User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }
            
            // Create request with userId from token
            val requestWithUserId = request.copy(userId = userId)
            
            val response = api.updateCategory(id, requestWithUserId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Update local database
                    try {
                        categoryDao.insertCategory(body.toEntity()) // Insert or replace
                        Log.d("CategoryRepo", "Category updated in local database")
                    } catch (e: Exception) {
                        Log.e("CategoryRepo", "Failed to update category in local database", e)
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
                Log.e("CategoryRepo", "Failed to update category: $errorMessage")
                Result.failure(Exception("Failed to update category: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("CategoryRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }
}
