package com.example.financemanagement.ui.budget

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.databinding.ItemBudgetBinding
import java.text.NumberFormat
import java.util.Locale

class BudgetAdapter(
    private val onEditClick: (BudgetItem) -> Unit,
    private val onDeleteClick: (BudgetItem) -> Unit
) : ListAdapter<BudgetItem, BudgetAdapter.BudgetViewHolder>(BudgetDiffCallback()) {

    private val numberFormat = NumberFormat.getInstance(Locale("vi", "VN"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val binding = ItemBudgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BudgetViewHolder(binding, onEditClick, onDeleteClick, numberFormat)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class BudgetViewHolder(
        private val binding: ItemBudgetBinding,
        private val onEditClick: (BudgetItem) -> Unit,
        private val onDeleteClick: (BudgetItem) -> Unit,
        private val numberFormat: NumberFormat
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BudgetItem) {
            binding.tvCategoryIcon.text = item.categoryIcon
            binding.tvCategoryName.text = item.categoryName
            
            // Display limit amount as formatted number
            binding.tvAmount.text = numberFormat.format(item.limitAmount.toLong())
            
            // Click listeners
            binding.tvEdit.setOnClickListener {
                onEditClick(item)
            }
            
            binding.ivArrow.setOnClickListener {
                onEditClick(item)
            }
            
            binding.ivDelete.setOnClickListener {
                onDeleteClick(item)
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
