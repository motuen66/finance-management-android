package com.example.financemanagement.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financemanagement.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>
    
    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgets(): List<BudgetEntity>
    
    @Query("SELECT * FROM budgets WHERE id = :id")
    suspend fun getBudgetById(id: String): BudgetEntity?
    
    @Query("SELECT * FROM budgets WHERE user_id = :userId")
    suspend fun getBudgetsByUserId(userId: String): List<BudgetEntity>
    
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    suspend fun getBudgetsByMonthYear(month: Int, year: Int): List<BudgetEntity>
    
    @Query("SELECT * FROM budgets WHERE category_id = :categoryId")
    suspend fun getBudgetsByCategoryId(categoryId: String): List<BudgetEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>): List<Long>
    
    @Update
    suspend fun updateBudget(budget: BudgetEntity): Int
    
    @Delete
    suspend fun deleteBudget(budget: BudgetEntity): Int
    
    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: String): Int
    
    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets(): Int
}
