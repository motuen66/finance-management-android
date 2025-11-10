package com.example.financemanagement.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.databinding.ItemCalendarDayBinding
import com.example.financemanagement.domain.model.CalendarDay

class CalendarAdapter(
    private val onDayClick: (CalendarDay) -> Unit
) : ListAdapter<CalendarDay, CalendarAdapter.DayViewHolder>(DayDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DayViewHolder(binding, onDayClick)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DayViewHolder(
        private val binding: ItemCalendarDayBinding,
        private val onDayClick: (CalendarDay) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(day: CalendarDay) {
            binding.tvDayNumber.text = day.dayOfMonth.toString()

            // Style for current month vs other months
            if (day.isCurrentMonth) {
                binding.tvDayNumber.setTextColor(Color.BLACK)
                binding.dayContainer.alpha = 1.0f
            } else {
                binding.tvDayNumber.setTextColor(Color.GRAY)
                binding.dayContainer.alpha = 0.5f
            }

            // Highlight today
            if (day.isToday) {
                binding.dayContainer.setBackgroundColor(Color.parseColor("#E3F2FD"))
            } else {
                binding.dayContainer.setBackgroundColor(Color.TRANSPARENT)
            }

            // Show income if > 0
            if (day.totalIncome > 0) {
                binding.tvIncome.visibility = View.VISIBLE
                binding.tvIncome.text = formatAmount(day.totalIncome)
            } else {
                binding.tvIncome.visibility = View.GONE
            }

            // Show expense if > 0
            if (day.totalExpense > 0) {
                binding.tvExpense.visibility = View.VISIBLE
                binding.tvExpense.text = formatAmount(day.totalExpense)
            } else {
                binding.tvExpense.visibility = View.GONE
            }

            // Click handler
            binding.root.setOnClickListener {
                if (day.isCurrentMonth) {
                    onDayClick(day)
                }
            }
        }

        private fun formatAmount(amount: Double): String {
            return when {
                amount >= 1_000_000_000 -> String.format("%.1fB", amount / 1_000_000_000)
                amount >= 1_000_000 -> String.format("%.0fM", amount / 1_000_000)
                amount >= 1_000 -> String.format("%.0fK", amount / 1_000)
                else -> String.format("%.0f", amount)
            }
        }
    }

    private class DayDiffCallback : DiffUtil.ItemCallback<CalendarDay>() {
        override fun areItemsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem.dayOfMonth == newItem.dayOfMonth &&
                    oldItem.month == newItem.month &&
                    oldItem.year == newItem.year
        }

        override fun areContentsTheSame(oldItem: CalendarDay, newItem: CalendarDay): Boolean {
            return oldItem == newItem
        }
    }
}
