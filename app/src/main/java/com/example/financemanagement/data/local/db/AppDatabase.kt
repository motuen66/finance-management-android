package com.example.financemanagement.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.financemanagement.data.local.dao.BudgetDao
import com.example.financemanagement.data.local.dao.CategoryDao
import com.example.financemanagement.data.local.dao.SavingGoalDao
import com.example.financemanagement.data.local.dao.TransactionDao
import com.example.financemanagement.data.local.dao.UserDao
import com.example.financemanagement.data.local.entities.BudgetEntity
import com.example.financemanagement.data.local.entities.CategoryEntity
import com.example.financemanagement.data.local.entities.SavingGoalEntity
import com.example.financemanagement.data.local.entities.TransactionEntity
import com.example.financemanagement.data.local.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SavingGoalEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingGoalDao(): SavingGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_management_database"
                )
                    .fallbackToDestructiveMigration() // Delete old db when update version
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}