package com.example.financemanagement.utils

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    
    /**
     * Format số tiền với K (nghìn) và M (triệu) để rút ngắn
     * Ví dụ:
     * - 500 → 500
     * - 5,000 → 5K
     * - 5,000,000 → 5M
     * - 15,500,000 → 16M
     */
    fun formatShort(amount: Double): String {
        return when {
            amount >= 1_000_000 -> {
                String.format("%.0fM", amount / 1_000_000)
            }
            amount >= 1_000 -> {
                String.format("%.0fK", amount / 1_000)
            }
            else -> {
                String.format("%.0f", amount)
            }
        }
    }
    
    /**
     * Format số tiền với K và M kèm đơn vị ₫
     */
    fun formatShortWithCurrency(amount: Double): String {
        return "${formatShort(amount)} ₫"
    }
    
    /**
     * Format số tiền đầy đủ với dấu phẩy ngăn cách (VN format)
     * Ví dụ: 1,500,000 ₫
     */
    fun formatFull(amount: Long): String {
        val format = NumberFormat.getInstance(Locale("vi", "VN"))
        return format.format(amount).replace("₫", "").trim() + " ₫"
    }
    
    /**
     * Format số tiền đầy đủ từ Double
     */
    fun formatFull(amount: Double): String {
        return formatFull(amount.toLong())
    }
    
    /**
     * Format số tiền với 1 chữ số thập phân cho M và K
     * Ví dụ: 5,500,000 → 5.5M
     */
    fun formatShortWithDecimal(amount: Double): String {
        return when {
            amount >= 1_000_000 -> {
                String.format("%.1fM", amount / 1_000_000)
            }
            amount >= 1_000 -> {
                String.format("%.1fK", amount / 1_000)
            }
            else -> {
                String.format("%.0f", amount)
            }
        }
    }
}
