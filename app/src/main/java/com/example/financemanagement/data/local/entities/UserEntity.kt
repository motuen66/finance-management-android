package com.example.financemanagement.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financemanagement.domain.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "email")
    val email: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: String? = null,
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: String? = null
)

// Mapper functions
fun UserEntity.toDomainModel() = User(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun User.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
    updatedAt = updatedAt
)
