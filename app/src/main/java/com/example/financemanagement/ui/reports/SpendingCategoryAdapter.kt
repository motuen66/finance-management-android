package com.example.financemanagement.ui.reports

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.databinding.ItemSpendingCategoryBinding
import com.example.financemanagement.viewmodel.SpendingCategory
import java.text.NumberFormat
import java.util.*

class SpendingCategoryAdapter(
    private val onItemClick: (SpendingCategory) -> Unit
) : ListAdapter<SpendingCategory, SpendingCategoryAdapter.ViewHolder>(DiffCallback()) {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSpendingCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    fun setSelectedPosition(position: Int) {
        val previousPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    inner class ViewHolder(
        private val binding: ItemSpendingCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position)
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(category: SpendingCategory, isSelected: Boolean) {
            binding.apply {
                tvCategoryName.text = category.name
                tvAmount.text = currencyFormatter.format(category.amount)
                tvPercentage.text = "${category.percent.toInt()}%"
                
                // Set icon
                ivCategoryIcon.setImageResource(category.iconRes)
                
                // Set icon color based on category color
                val iconColor = category.color
                ivCategoryIcon.setColorFilter(iconColor)
                
                // Set icon container background color (lighter version)
                val lightColor = adjustAlpha(iconColor, 0.15f)
                iconContainer.setCardBackgroundColor(lightColor)
                
                // Highlight selected card
                if (isSelected) {
                    cardCategory.strokeWidth = 4
                    cardCategory.strokeColor = category.color
                    cardCategory.cardElevation = 8f
                } else {
                    cardCategory.strokeWidth = 0
                    cardCategory.cardElevation = 2f
                }
            }
        }

        private fun adjustAlpha(color: Int, factor: Float): Int {
            val alpha = (Color.alpha(color) * factor).toInt()
            val red = Color.red(color)
            val green = Color.green(color)
            val blue = Color.blue(color)
            return Color.argb(alpha, red, green, blue)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<SpendingCategory>() {
        override fun areItemsTheSame(oldItem: SpendingCategory, newItem: SpendingCategory): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: SpendingCategory, newItem: SpendingCategory): Boolean {
            return oldItem == newItem
        }
    }
}
