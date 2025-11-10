package com.example.financemanagement.ui.budget

import com.example.financemanagement.domain.model.Budget
import com.example.financemanagement.domain.model.Category

data class BudgetItem(
    val budgetId: String?, // Null if budget not created yet
    val category: Category,
    val categoryName: String,
    val categoryIcon: String,
    val limitAmount: Double, // 0 if not set yet
    val month: Int,
    val year: Int
) {
    companion object {
        /**
         * Create BudgetItem from expense category and optional budget
         * If budget doesn't exist yet for this category, limitAmount will be 0
         */
        fun fromCategory(
            category: Category,
            budget: Budget?,
            currentMonth: Int,
            currentYear: Int
        ): BudgetItem {
            val categoryName = category.name
            val categoryIcon = getCategoryIcon(categoryName)
            
            return BudgetItem(
                budgetId = budget?.id,
                category = category,
                categoryName = categoryName,
                categoryIcon = categoryIcon,
                limitAmount = budget?.limitAmount ?: 0.0,
                month = budget?.month ?: currentMonth,
                year = budget?.year ?: currentYear
            )
        }

        private fun getCategoryIcon(categoryName: String): String {
            return when (categoryName.lowercase()) {
                "food" -> "🍔"
                "vegetable" -> "🥬"
                "fruit" -> "🍓"
                "fuel" -> "⛽"
                "shopping" -> "🛒"
                "cigarette" -> "📦"
                "tobacco" -> "🍖"
                "alcoholic beverages" -> "🍺"
                "telephone" -> "📱"
                "transportation" -> "🎨"
                else -> "💰"
            }
        }
    }
}
