package com.example.financemanagement.ui.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.databinding.ItemCategoryBinding
import com.example.financemanagement.domain.model.Category

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            
            // Set icon and badge based on category type
            val context = binding.root.context
            val isIncome = category.type.equals("Income", ignoreCase = true)
            
            if (isIncome) {
                binding.ivCategoryIcon.setImageResource(com.example.financemanagement.R.drawable.ic_income)
                binding.tvCategoryType.text = context.getString(com.example.financemanagement.R.string.type_income)
                binding.tvCategoryType.setBackgroundResource(com.example.financemanagement.R.drawable.badge_income)
            } else {
                binding.ivCategoryIcon.setImageResource(com.example.financemanagement.R.drawable.ic_expense)
                binding.tvCategoryType.text = context.getString(com.example.financemanagement.R.string.type_expense)
                binding.tvCategoryType.setBackgroundResource(com.example.financemanagement.R.drawable.badge_expense)
            }
            
            // Set click listeners for edit and delete buttons
            binding.btnEdit.setOnClickListener {
                onItemClick(category)
            }
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(category)
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
