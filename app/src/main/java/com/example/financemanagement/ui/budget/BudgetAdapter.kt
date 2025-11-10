package com.example.financemanagement.ui.budget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.databinding.ItemBudgetBinding
import com.example.financemanagement.utils.CurrencyFormatter

class BudgetAdapter(
    private val onEditClick: (BudgetItem) -> Unit,
    private val onDeleteClick: (BudgetItem) -> Unit,
    private val onItemClick: ((BudgetItem) -> Unit)? = null
) : ListAdapter<BudgetItem, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BudgetViewHolder(binding, onEditClick, onDeleteClick, onItemClick)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BudgetViewHolder(
        private val binding: ItemBudgetBinding,
        private val onEditClick: (BudgetItem) -> Unit,
        private val onDeleteClick: (BudgetItem) -> Unit,
        private val onItemClick: ((BudgetItem) -> Unit)?
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BudgetItem) {
            binding.tvCategoryIcon.text = item.categoryIcon
            binding.tvCategoryName.text = item.categoryName
            
            // Display limit amount với format ngắn (K/M)
            binding.tvAmount.text = CurrencyFormatter.formatShortWithCurrency(item.limitAmount)
            
            // Click listeners
            binding.tvEdit.setOnClickListener {
                onEditClick(item)
            }
            
            // Item click listener
            binding.root.setOnClickListener {
                onItemClick?.invoke(item)
            }

        }
    }

    private class BudgetDiffCallback : DiffUtil.ItemCallback<BudgetItem>() {
        override fun areItemsTheSame(oldItem: BudgetItem, newItem: BudgetItem): Boolean {
            // Compare by category ID since that's the unique identifier
            return oldItem.category.id == newItem.category.id
        }

        override fun areContentsTheSame(oldItem: BudgetItem, newItem: BudgetItem): Boolean {
            return oldItem == newItem
        }
    }
}
