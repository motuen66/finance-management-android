package com.example.financemanagement.data.repository

import com.example.financemanagement.domain.model.SavingGoal
import com.example.financemanagement.domain.model.SavingContribution
import kotlinx.coroutines.flow.Flow

interface SavingGoalRepository {
    // Saving Goals operations
    suspend fun getSavingGoals(): Result<List<SavingGoal>>
    suspend fun getSavingGoalById(id: String): Result<SavingGoal>
    suspend fun createSavingGoal(goal: SavingGoal): Result<SavingGoal>
    suspend fun updateSavingGoal(goal: SavingGoal): Result<SavingGoal>
    suspend fun deleteSavingGoal(goalId: String): Result<Unit>
    fun observeSavingGoals(): Flow<List<SavingGoal>>
    
    // Contributions operations  
    suspend fun addContribution(contribution: SavingContribution): Result<SavingContribution>
    suspend fun getContributionsByGoalId(goalId: String): Result<List<SavingContribution>>
    suspend fun deleteContribution(contributionId: String): Result<Unit>
    fun observeContributionsByGoalId(goalId: String): Flow<List<SavingContribution>>
    
    // Helper operations
    suspend fun updateGoalProgress(goalId: String): Result<SavingGoal>
}
