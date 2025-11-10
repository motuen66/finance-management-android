package com.example.financemanagement.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financemanagement.databinding.ItemTransactionDateGroupBinding
import com.example.financemanagement.domain.model.TransactionGroup

class TransactionGroupAdapter(private val onTransactionClick: (com.example.financemanagement.domain.model.Transaction) -> Unit) :
    RecyclerView.Adapter<TransactionGroupAdapter.GroupViewHolder>() {

    private val groups = mutableListOf<TransactionGroup>()

    fun submitList(newGroups: List<TransactionGroup>) {
        groups.clear()
        groups.addAll(newGroups)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = ItemTransactionDateGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    inner class GroupViewHolder(private val binding: ItemTransactionDateGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(group: TransactionGroup) {
            binding.tvDateHeader.text = group.dateHeader

            // Setup nested RecyclerView for transactions
            val transactionAdapter = TransactionAdapter(onTransactionClick)
            binding.rvTransactions.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = transactionAdapter
            }
            transactionAdapter.submitList(group.transactions)
        }
    }
}
