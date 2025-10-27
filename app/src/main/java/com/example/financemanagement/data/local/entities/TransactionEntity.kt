package com.example.financemanagement.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financemanagement.domain.model.Transaction

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "note")
    val note: String,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "date")
    val date: String,
    
    @ColumnInfo(name = "user_id")
    val userId: String,
    
    @ColumnInfo(name = "type")
    val type: String, // "Income" or "Expense"
    
    @ColumnInfo(name = "category_id")
    val categoryId: String
)

// Mapper functions
fun TransactionEntity.toDomainModel() = Transaction(
    id = id,
    note = note,
    amount = amount,
    date = date,
    userId = userId,
    type = type,
    categoryId = categoryId,
    category = null // Category sẽ được load riêng nếu cần
)

fun Transaction.toEntity() = TransactionEntity(
    id = id,
    note = note,
    amount = amount,
    date = date,
    userId = userId,
    type = type,
    categoryId = categoryId
)
