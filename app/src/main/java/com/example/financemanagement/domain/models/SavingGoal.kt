package com.example.financemanagement.domain.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class SavingGoal(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("userId")
    val userId: String? = null,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("goalAmount")
    val goalAmount: Double,
    
    @SerializedName("currentAmount")
    val currentAmount: Double = 0.0,
    
    @SerializedName("goalDate")
    val goalDate: String, // ISO format: yyyy-MM-dd
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("isCompleted")
    val isCompleted: Boolean = false
) {
    // Progress as percentage (0.0 to 1.0)
    val progress: Float 
        get() = if (goalAmount > 0) (currentAmount / goalAmount).toFloat().coerceIn(0f, 1f) else 0f
    
    // Remaining amount to reach goal
    val remainingAmount: Double 
        get() = maxOf(0.0, goalAmount - currentAmount)
    
    // Check if goal is achieved
    val isAchieved: Boolean 
        get() = currentAmount >= goalAmount
    
    // Days remaining until target date
    val daysRemaining: Long?
        get() = try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = formatter.parse(goalDate)
            val currentDate = Date()
            if (targetDate != null) {
                val diffInMillis = targetDate.time - currentDate.time
                diffInMillis / (1000 * 60 * 60 * 24)
            } else null
        } catch (e: Exception) {
            null
        }
    
    // Check if goal is overdue
    val isOverdue: Boolean
        get() = daysRemaining?.let { it < 0 && !isAchieved } ?: false
}
