package com.example.financemanagement.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import com.example.financemanagement.data.local.entities.SavingContributionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingContributionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: SavingContributionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContributions(contributions: List<SavingContributionEntity>)
    
    @Update
    suspend fun updateContribution(contribution: SavingContributionEntity)
    
    @Delete
    suspend fun deleteContribution(contribution: SavingContributionEntity)
    
    @Query("DELETE FROM saving_contributions WHERE id = :contributionId")
    suspend fun deleteContributionById(contributionId: String)
    
    @Query("SELECT * FROM saving_contributions WHERE goal_id = :goalId ORDER BY created_at DESC")
    fun getContributionsByGoalId(goalId: String): Flow<List<SavingContributionEntity>>
    
    @Query("SELECT * FROM saving_contributions WHERE goal_id = :goalId ORDER BY created_at DESC")
    suspend fun getContributionsByGoalIdOnce(goalId: String): List<SavingContributionEntity>
    
    @Query("SELECT * FROM saving_contributions WHERE id = :contributionId")
    suspend fun getContributionById(contributionId: String): SavingContributionEntity?
    
    @Query("SELECT SUM(amount) FROM saving_contributions WHERE goal_id = :goalId")
    suspend fun getTotalContributionsForGoal(goalId: String): Double?
    
    @Query("SELECT COUNT(*) FROM saving_contributions WHERE goal_id = :goalId")
    suspend fun getContributionsCountForGoal(goalId: String): Int
    
    @Query("DELETE FROM saving_contributions WHERE goal_id = :goalId")
    suspend fun deleteAllContributionsForGoal(goalId: String)
    
    @Query("DELETE FROM saving_contributions")
    suspend fun deleteAllContributions()
}