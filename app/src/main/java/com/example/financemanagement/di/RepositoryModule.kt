package com.example.financemanagement.di

import com.example.financemanagement.data.repository.AuthRepository
import com.example.financemanagement.data.repository.AuthRepositoryImpl
import com.example.financemanagement.data.repository.SavingGoalRepository
import com.example.financemanagement.data.repository.SavingGoalRepositoryImpl
import com.example.financemanagement.data.repository.TransactionRepository
import com.example.financemanagement.data.repository.TransactionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindSavingGoalRepository(impl: SavingGoalRepositoryImpl): SavingGoalRepository
}
