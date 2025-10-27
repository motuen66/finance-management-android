package com.example.financemanagement.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financemanagement.domain.model.Budget

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    
    @ColumnInfo(name = "limit_amount")
    val limitAmount: Double,
    
    @ColumnInfo(name = "month")
    val month: Int,
    
    @ColumnInfo(name = "year")
    val year: Int
)

// Mapper functions
fun BudgetEntity.toDomainModel() = Budget(
    id = id,
    userId = userId,
    categoryId = categoryId,
    limitAmount = limitAmount,
    month = month,
    year = year
)

fun Budget.toEntity() = BudgetEntity(
    id = id,
    userId = userId,
    categoryId = categoryId,
    limitAmount = limitAmount,
    month = month,
    year = year
)
