package com.example.financemanagement.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financemanagement.data.local.entities.SavingGoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {
    @Query("SELECT * FROM saving_goals")
    fun observeAll(): Flow<List<SavingGoalEntity>>
    
    @Query("SELECT * FROM saving_goals")
    suspend fun getAllSavingGoals(): List<SavingGoalEntity>
    
    @Query("SELECT * FROM saving_goals WHERE id = :id")
    suspend fun getSavingGoalById(id: String): SavingGoalEntity?
    
    @Query("SELECT * FROM saving_goals WHERE user_id = :userId")
    suspend fun getSavingGoalsByUserId(userId: String?): List<SavingGoalEntity>
    
    @Query("SELECT * FROM saving_goals WHERE is_completed = :isCompleted")
    suspend fun getSavingGoalsByStatus(isCompleted: Boolean): List<SavingGoalEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingGoal(savingGoal: SavingGoalEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingGoals(savingGoals: List<SavingGoalEntity>): List<Long>
    
    @Update
    suspend fun updateSavingGoal(savingGoal: SavingGoalEntity): Int
    
    @Delete
    suspend fun deleteSavingGoal(savingGoal: SavingGoalEntity): Int
    
    @Query("DELETE FROM saving_goals WHERE id = :id")
    suspend fun deleteSavingGoalById(id: String): Int
    
    @Query("DELETE FROM saving_goals")
    suspend fun deleteAllSavingGoals(): Int
}
