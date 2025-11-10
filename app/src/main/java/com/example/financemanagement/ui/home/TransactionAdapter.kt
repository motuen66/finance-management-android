package com.example.financemanagement.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.databinding.ItemTransactionBinding
import com.example.financemanagement.domain.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val onTransactionClick: (Transaction) -> Unit) :
    ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTransactionClick(getItem(position))
                }
            }
        }

        fun bind(transaction: Transaction) {
            // Set category name from API response
            binding.tvCategoryName.text = transaction.categoryName ?: "Unknown Category"

            // Set note
//            binding.tvNote.text = transaction.note.ifEmpty { "No note" }

            // Format time from date
            binding.tvTime.text = formatTime(transaction.date)

            // Set amount and color based on type
            val amount = transaction.amount
            val formattedAmount = if (transaction.type == "Income") {
                "+${formatAmount(amount)}"
            } else {
                "-${formatAmount(amount)}"
            }
            binding.tvAmount.text = formattedAmount
            
            // Set color based on type
            val color = if (transaction.type == "Income") {
                0xFF4CAF50.toInt() // Green for income
            } else {
                0xFFF44336.toInt() // Red for expense
            }
            binding.tvAmount.setTextColor(color)

            // Set icon based on category name
            binding.tvIcon.text = getCategoryIcon(transaction.categoryName, transaction.type)
        }

        private fun getCategoryIcon(categoryName: String?, type: String): String {
            return when {
                categoryName?.contains("Food", ignoreCase = true) == true -> "🍔"
                categoryName?.contains("Salary", ignoreCase = true) == true -> "💰"
                categoryName?.contains("Transport", ignoreCase = true) == true -> "🚗"
                categoryName?.contains("Shopping", ignoreCase = true) == true -> "🛒"
                categoryName?.contains("Health", ignoreCase = true) == true -> "💊"
                categoryName?.contains("Entertainment", ignoreCase = true) == true -> "🎮"
                categoryName?.contains("Bill", ignoreCase = true) == true -> "📄"
                type == "Income" -> "�"
                type == "Expense" -> "�"
                else -> "�"
            }
        }

        private fun formatTime(dateStr: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val date = inputFormat.parse(dateStr)
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.US)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                "N/A"
            }
        }

        private fun formatAmount(amount: Double): String {
            return if (amount >= 1_000_000) {
                String.format("%.0fM", amount / 1_000_000)
            } else if (amount >= 1_000) {
                String.format("%.0fK", amount / 1_000)
            } else {
                String.format("%.0f", amount)
            }
        }
    }

    class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
