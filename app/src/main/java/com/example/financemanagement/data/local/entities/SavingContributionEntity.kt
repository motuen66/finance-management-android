package com.example.financemanagement.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.financemanagement.domain.model.SavingContribution

@Entity(
    tableName = "saving_contributions",
    indices = [
        Index(value = ["goal_id"])
    ]
)
data class SavingContributionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "goal_id")
    val goalId: String,
    
    @ColumnInfo(name = "amount")
    val amount: Double,
    
    @ColumnInfo(name = "note")
    val note: String?,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String
)

// Mapper functions
fun SavingContributionEntity.toDomainModel() = SavingContribution(
    id = id,
    goalId = goalId,
    amount = amount,
    note = note,
    createdAt = createdAt
)

fun SavingContribution.toEntity() = SavingContributionEntity(
    id = id,
    goalId = goalId,
    amount = amount,
    note = note,
    createdAt = createdAt
)