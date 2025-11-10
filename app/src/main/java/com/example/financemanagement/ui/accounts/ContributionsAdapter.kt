package com.example.financemanagement.ui.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.databinding.ItemContributionBinding
import com.example.financemanagement.domain.model.SavingContribution
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ContributionsAdapter(
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<SavingContribution, ContributionsAdapter.ContributionViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributionViewHolder {
        val binding = ItemContributionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContributionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContributionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContributionViewHolder(
        private val binding: ItemContributionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contribution: SavingContribution) {
            binding.apply {
                // Amount (no currency symbol)
                val currencyFormat = NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
                tvAmount.text = "+${currencyFormat.format(contribution.amount)} đ"

                // Date
                val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                try {
                    val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val date = isoFormatter.parse(contribution.createdAt)
                    tvDate.text = if (date != null) {
                        dateFormatter.format(date)
                    } else {
                        "Unknown date"
                    }
                } catch (e: Exception) {
                    // Fallback for different date formats
                    try {
                        val simpleFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val date = simpleFormatter.parse(contribution.createdAt)
                        tvDate.text = if (date != null) {
                            dateFormatter.format(date)
                        } else {
                            "Unknown date"
                        }
                    } catch (e2: Exception) {
                        tvDate.text = contribution.createdAt
                    }
                }

                // Note
                tvNote.apply {
                    if (!contribution.note.isNullOrEmpty()) {
                        text = contribution.note
                        visibility = View.VISIBLE
                    } else {
                        visibility = View.GONE
                    }
                }

                // Delete button (show on long press)
                var isDeleteVisible = false
                root.setOnLongClickListener {
                    isDeleteVisible = !isDeleteVisible
                    btnDelete.isVisible = isDeleteVisible
                    true
                }

                btnDelete.setOnClickListener {
                    onDeleteClick(contribution.id)
                }
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SavingContribution>() {
        override fun areItemsTheSame(oldItem: SavingContribution, newItem: SavingContribution): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavingContribution, newItem: SavingContribution): Boolean {
            return oldItem == newItem
        }
    }
}