package com.example.financemanagement.data.repository

import com.example.financemanagement.data.remote.models.CategoryRequest
import com.example.financemanagement.domain.model.Category

interface CategoryRepository {
    suspend fun getCategories(): Result<List<Category>>
    suspend fun getCategoryById(id: String): Result<Category>
    suspend fun createCategory(request: CategoryRequest): Result<Category>
    suspend fun updateCategory(id: String, request: CategoryRequest): Result<Category>
    suspend fun createCategory(name: String, type: String): Result<Category>
    suspend fun updateCategory(id: String, name: String, type: String): Result<Category>
    suspend fun deleteCategory(id: String): Result<Unit>
}
