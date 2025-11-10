package com.example.financemanagement.data.repository

import android.util.Log
import com.example.financemanagement.data.remote.ApiService
import com.example.financemanagement.data.remote.models.TransactionRequest
import com.example.financemanagement.domain.model.Transaction
import java.io.IOException
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val api: ApiService
) : TransactionRepository {

    override suspend fun getTransactions(): Result<List<Transaction>> {
        return try {
            val response = api.getTransactions()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("TransactionRepo", "Failed to fetch transactions: $errorMessage")
                Result.failure(Exception("Failed to fetch transactions: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("TransactionRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun createTransaction(request: TransactionRequest): Result<Transaction> {
        return try {
            Log.d("TransactionRepo", "Creating transaction: $request")
            val response = api.createTransaction(request)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d("TransactionRepo", "Transaction created successfully: ${body.id}")
                    Result.success(body)
                } else {
                    Log.e("TransactionRepo", "Empty response body")
                    Result.failure(Exception("Empty response body"))
                }
            } else {
                val errorMessage = try {
                    response.errorBody()?.string() ?: response.message()
                } catch (e: Exception) {
                    response.message()
                }
                Log.e("TransactionRepo", "Failed to create transaction: $errorMessage")
                Result.failure(Exception("Failed to create transaction: $errorMessage"))
            }
        } catch (e: IOException) {
            Log.e("TransactionRepo", "Network error creating transaction", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("TransactionRepo", "Unknown error creating transaction", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }
}
