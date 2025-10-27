package com.example.financemanagement.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financemanagement.domain.model.SavingGoal

@Entity(tableName = "saving_goals")
data class SavingGoalEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String?,
    
    @ColumnInfo(name = "title")
    val title: String,
    
    @ColumnInfo(name = "goal_amount")
    val goalAmount: Double,
    
    @ColumnInfo(name = "current_amount")
    val currentAmount: Double,
    
    @ColumnInfo(name = "goal_date")
    val goalDate: String,
    
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean
)

// Mapper functions
fun SavingGoalEntity.toDomainModel() = SavingGoal(
    id = id,
    userId = userId,
    title = title,
    goalAmount = goalAmount,
    currentAmount = currentAmount,
    goalDate = goalDate,
    isCompleted = isCompleted
)

fun SavingGoal.toEntity() = SavingGoalEntity(
    id = id,
    userId = userId,
    title = title,
    goalAmount = goalAmount,
    currentAmount = currentAmount,
    goalDate = goalDate,
    isCompleted = isCompleted
)
