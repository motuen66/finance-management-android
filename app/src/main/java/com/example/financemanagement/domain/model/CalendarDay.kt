package com.example.financemanagement.domain.model

data class CalendarDay(
    val dayOfMonth: Int,
    val month: Int,
    val year: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0
) {
    fun getDateString(): String {
        return String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
    }
}
