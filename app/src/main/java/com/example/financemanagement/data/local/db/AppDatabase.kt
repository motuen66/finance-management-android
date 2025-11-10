package com.example.financemanagement.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.financemanagement.data.local.dao.TransactionDao
import com.example.financemanagement.data.local.dao.UserDao
import com.example.financemanagement.data.local.models.TransactionEntity
import com.example.financemanagement.data.local.models.UserEntity
import android.content.Context

@Database(entities = [UserEntity::class, TransactionEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_management_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}