package com.example.financemanagement.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financemanagement.domain.model.Category

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "type")
    val type: String, // "Income" or "Expense"
    
    @ColumnInfo(name = "user_id")
    val userId: String
)

// Mapper functions
fun CategoryEntity.toDomainModel() = Category(
    id = id,
    name = name,
    type = type,
    userId = userId
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    name = name,
    type = type,
    userId = userId
)
