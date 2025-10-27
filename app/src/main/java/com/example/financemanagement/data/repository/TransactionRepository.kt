package com.example.financemanagement.data.repository

import com.example.financemanagement.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(): Result<List<Transaction>>
}
