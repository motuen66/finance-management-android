package com.example.financemanagement.data.repository

import com.example.financemanagement.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    suspend fun getBudgetsByUserId(userId: String): Result<List<Budget>>
}