package com.example.financemanagement.data.repository

<<<<<<< Updated upstream
import com.example.financemanagement.data.remote.models.CategoryRequest
=======
>>>>>>> Stashed changes
import com.example.financemanagement.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getCategoryById(id: String): Result<Category>
<<<<<<< Updated upstream
    suspend fun createCategory(request: CategoryRequest): Result<Category>
    suspend fun updateCategory(id: String, request: CategoryRequest): Result<Category>
=======
    suspend fun createCategory(name: String, type: String): Result<Category>
    suspend fun updateCategory(id: String, name: String, type: String): Result<Category>
>>>>>>> Stashed changes
    suspend fun deleteCategory(id: String): Result<Unit>
}
