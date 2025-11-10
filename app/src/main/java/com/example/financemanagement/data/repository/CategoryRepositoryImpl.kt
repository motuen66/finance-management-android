package com.example.financemanagement.data.repository

import android.util.Log
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
    private val categoryDao: CategoryDao
) : CategoryRepository {

    companion object {
        private const val HARDCODED_USER_ID = "6904cf1320173db06b2641b8"
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
                        Log.e("CategoryRepo", "Failed to cache categories locally", e)
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
                Log.e("CategoryRepo", "Failed to fetch categories: $errorMessage")
                Result.failure(Exception("Failed to fetch categories: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("CategoryRepo", "Network error", e)
            // Try to get from local database if network fails
            try {
                val localCategories = categoryDao.getAllCategories()
                if (localCategories.isNotEmpty()) {
                    Log.d("CategoryRepo", "Using cached categories from local database")
                    return Result.success(localCategories.map { it.toDomainModel() })
                }
            } catch (dbError: Exception) {
                Log.e("CategoryRepo", "Failed to get categories from local database", dbError)
            }
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
                    // Update local cache
                    try {
                        categoryDao.insertCategory(body.toEntity())
                    } catch (e: Exception) {
                        Log.e("CategoryRepo", "Failed to cache category locally", e)
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
                Log.e("CategoryRepo", "Failed to fetch category: $errorMessage")
                Result.failure(Exception("Failed to fetch category: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("CategoryRepo", "Network error", e)
            // Try to get from local database if network fails
            try {
                val localCategory = categoryDao.getCategoryById(id)
                if (localCategory != null) {
                    Log.d("CategoryRepo", "Using cached category from local database")
                    return Result.success(localCategory.toDomainModel())
                }
            } catch (dbError: Exception) {
                Log.e("CategoryRepo", "Failed to get category from local database", dbError)
            }
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("CategoryRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun createCategory(request: CategoryRequest): Result<Category> {
        return try {
            // Create request with hardcoded userId
            val requestWithUserId = request.copy(userId = HARDCODED_USER_ID)
            
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
            // Create request with hardcoded userId
            val requestWithUserId = request.copy(userId = HARDCODED_USER_ID)
            
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

    override suspend fun deleteCategory(id: String): Result<Unit> {
        return try {
            val response = api.deleteCategory(id)
            
            if (response.isSuccessful) {
                // Delete from local database
                try {
                    categoryDao.deleteCategoryById(id)
                    Log.d("CategoryRepo", "Category deleted from local database")
                } catch (e: Exception) {
                    Log.e("CategoryRepo", "Failed to delete category from local database", e)
                }
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
}
