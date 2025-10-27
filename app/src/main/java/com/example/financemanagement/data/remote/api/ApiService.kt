package com.example.financemanagement.data.remote

import com.example.financemanagement.data.remote.models.*
import com.example.financemanagement.domain.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // ========== AUTH ==========
    @POST("api/Account/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/Account/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // ========== TRANSACTIONS ==========
    @GET("api/Transactions")
    suspend fun getTransactions(): Response<List<Transaction>>

    @GET("api/Transactions/{id}")
    suspend fun getTransactionById(@Path("id") id: String): Response<Transaction>

    @POST("api/Transactions")
    suspend fun createTransaction(@Body request: TransactionRequest): Response<Transaction>

    @PUT("api/Transactions/{id}")
    suspend fun updateTransaction(
        @Path("id") id: String,
        @Body request: TransactionRequest
    ): Response<Transaction>

    @DELETE("api/Transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): Response<Unit>

    // ========== CATEGORIES ==========
    @GET("api/Categories")
    suspend fun getCategories(): Response<List<Category>>

    @GET("api/Categories/{id}")
    suspend fun getCategoryById(@Path("id") id: String): Response<Category>

    @POST("api/Categories")
    suspend fun createCategory(@Body request: CategoryRequest): Response<Category>

    @PUT("api/Categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: CategoryRequest
    ): Response<Category>

    @DELETE("api/Categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<Unit>

    // ========== BUDGETS ==========
    @GET("api/Budgets")
    suspend fun getBudgets(): Response<List<Budget>>

    @GET("api/Budgets/{id}")
    suspend fun getBudgetById(@Path("id") id: String): Response<Budget>

    @POST("api/Budgets")
    suspend fun createBudget(@Body request: BudgetRequest): Response<Budget>

    @PUT("api/Budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: String,
        @Body request: BudgetRequest
    ): Response<Budget>

    @DELETE("api/Budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: String): Response<Unit>

    // ========== SAVING GOALS ==========
    @GET("api/SavingGoals")
    suspend fun getSavingGoals(): Response<List<SavingGoal>>

    @GET("api/SavingGoals/{id}")
    suspend fun getSavingGoalById(@Path("id") id: String): Response<SavingGoal>

    @POST("api/SavingGoals")
    suspend fun createSavingGoal(@Body request: SavingGoalRequest): Response<SavingGoal>

    @PUT("api/SavingGoals/{id}")
    suspend fun updateSavingGoal(
        @Path("id") id: String,
        @Body request: SavingGoalRequest
    ): Response<SavingGoal>

    @DELETE("api/SavingGoals/{id}")
    suspend fun deleteSavingGoal(@Path("id") id: String): Response<Unit>

    // ========== REPORTS ==========
    @GET("api/Reports")
    suspend fun getReports(): Response<List<Report>>

    @POST("api/Reports/generate")
    suspend fun generateReport(@Body request: ReportRequest): Response<Report>

    @GET("api/Reports/{id}")
    suspend fun getReportById(@Path("id") id: String): Response<Report>

    // ========== USER ==========
    @GET("api/Users/me")
    suspend fun getCurrentUser(): Response<User>

    @PUT("api/Users/me")
    suspend fun updateUser(@Body user: User): Response<User>
}
