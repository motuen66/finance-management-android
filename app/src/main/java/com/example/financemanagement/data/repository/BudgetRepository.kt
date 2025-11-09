package com.example.financemanagement.data.repository

import com.example.financemanagement.data.remote.models.BudgetRequest
import com.example.financemanagement.domain.model.Budget
import com.example.financemanagement.domain.model.Category

interface BudgetRepository {
    suspend fun getBudgets(): Result<List<Budget>>
    suspend fun getBudgetById(id: String): Result<Budget>
    suspend fun createBudget(request: BudgetRequest): Result<Budget>
    suspend fun updateBudget(id: String, request: BudgetRequest): Result<Budget>
    suspend fun deleteBudget(id: String): Result<Unit>
    suspend fun getCategories(): Result<List<Category>>
}
