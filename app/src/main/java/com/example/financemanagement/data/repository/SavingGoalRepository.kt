package com.example.financemanagement.data.repository

import com.example.financemanagement.domain.model.SavingGoal

interface SavingGoalRepository {
    suspend fun getSavingGoals(): Result<List<SavingGoal>>
}
