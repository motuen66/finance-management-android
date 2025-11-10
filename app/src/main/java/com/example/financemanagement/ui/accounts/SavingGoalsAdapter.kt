package com.example.financemanagement.ui.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.databinding.ItemSavingGoalBinding
import com.example.financemanagement.domain.model.SavingGoal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class SavingGoalsAdapter(
    private val onAddContributionClick: (SavingGoal) -> Unit,
    private val onViewDetailsClick: (SavingGoal) -> Unit,
    private val onMenuClick: (SavingGoal, View) -> Unit
) : ListAdapter<SavingGoal, SavingGoalsAdapter.GoalViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemSavingGoalBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GoalViewHolder(
        private val binding: ItemSavingGoalBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(goal: SavingGoal) {
            binding.apply {
                // Basic info
                tvGoalTitle.text = goal.title
                tvGoalDescription.isVisible = !goal.description.isNullOrEmpty()
                tvGoalDescription.text = goal.description

                // Progress
                val progressPercentage = (goal.progress * 100).toInt()
                progressBar.progress = progressPercentage
                tvProgressPercentage.text = "$progressPercentage%"

                // Amounts
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
                tvCurrentAmount.text = currencyFormat.format(goal.currentAmount)
                tvTargetAmount.text = "of ${currencyFormat.format(goal.goalAmount)}"

                // Target date
                val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                try {
                    val targetDateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val targetDate = targetDateFormatter.parse(goal.goalDate)
                    tvTargetDate.text = if (targetDate != null) {
                        "Target: ${dateFormatter.format(targetDate)}"
                    } else {
                        "Target: ${goal.goalDate}"
                    }
                } catch (e: Exception) {
                    tvTargetDate.text = "Target: ${goal.goalDate}"
                }

                // Status
                when {
                    goal.isAchieved -> {
                        tvStatus.isVisible = true
                        tvStatus.text = "COMPLETED"
                        tvStatus.setBackgroundResource(android.R.drawable.btn_default)
                    }
                    goal.isOverdue -> {
                        tvStatus.isVisible = true
                        tvStatus.text = "OVERDUE"
                        tvStatus.setBackgroundResource(android.R.drawable.btn_default)
                    }
                    else -> {
                        tvStatus.isVisible = false
                    }
                }

                // Click listeners
                btnAddContribution.setOnClickListener {
                    onAddContributionClick(goal)
                }

                btnViewDetails.setOnClickListener {
                    onViewDetailsClick(goal)
                }

                btnMenu.setOnClickListener {
                    onMenuClick(goal, it)
                }

                // Disable add contribution if goal is completed
                btnAddContribution.isEnabled = !goal.isAchieved
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SavingGoal>() {
        override fun areItemsTheSame(oldItem: SavingGoal, newItem: SavingGoal): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavingGoal, newItem: SavingGoal): Boolean {
            return oldItem == newItem
        }
    }
}