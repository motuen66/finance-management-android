package com.example.financemanagement.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Empty Hilt module placeholder. Actual providers live in `NetworkModule` and
 * `RepositoryModule`. This file prevents accidental generation of stubs with
 * unresolved references when other modules are refactored.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // intentionally empty
}
