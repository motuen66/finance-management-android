package com.example.financemanagement.data.repository

import com.example.financemanagement.data.remote.models.TransactionRequest
import com.example.financemanagement.domain.model.Transaction

interface TransactionRepository {
    suspend fun getTransactions(): Result<List<Transaction>>
    suspend fun createTransaction(request: TransactionRequest): Result<Transaction>
}
