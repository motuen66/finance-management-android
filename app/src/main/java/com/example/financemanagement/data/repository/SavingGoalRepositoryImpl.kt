package com.example.financemanagement.data.repository

import android.util.Log
import com.example.financemanagement.data.local.dao.SavingGoalDao
import com.example.financemanagement.data.local.dao.SavingContributionDao
import com.example.financemanagement.data.local.entities.toDomainModel
import com.example.financemanagement.data.local.entities.toEntity
import com.example.financemanagement.data.remote.api.ApiService
import com.example.financemanagement.domain.model.SavingGoal
import com.example.financemanagement.domain.model.SavingContribution
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SavingGoalRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val savingGoalDao: SavingGoalDao,
    private val contributionDao: SavingContributionDao
) : SavingGoalRepository {

    override suspend fun getSavingGoals(): Result<List<SavingGoal>> {
        return try {
            val response = api.getSavingGoals()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Cache data locally
                    val entities = body.map { it.toEntity() }
                    savingGoalDao.insertSavingGoals(entities)
                    Result.success(body)
                } else {
                    // Fallback to local data
                    val localGoals = savingGoalDao.getAllSavingGoals().map { it.toDomainModel() }
                    Result.success(localGoals)
                }
            } else {
                // Fallback to local data
                val localGoals = savingGoalDao.getAllSavingGoals().map { it.toDomainModel() }
                Result.success(localGoals)
            }
        } catch (e: IOException) {
            Log.e("SavingGoalRepo", "Network error", e)
            // Fallback to local data
            val localGoals = savingGoalDao.getAllSavingGoals().map { it.toDomainModel() }
            Result.success(localGoals)
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun getSavingGoalById(id: String): Result<SavingGoal> {
        return try {
            val response = api.getSavingGoalById(id)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Cache locally
                    savingGoalDao.insertSavingGoal(body.toEntity())
                    Result.success(body)
                } else {
                    Result.failure(Exception("Goal not found"))
                }
            } else {
                // Fallback to local data
                val localGoal = savingGoalDao.getSavingGoalById(id)?.toDomainModel()
                if (localGoal != null) {
                    Result.success(localGoal)
                } else {
                    Result.failure(Exception("Goal not found"))
                }
            }
        } catch (e: IOException) {
            Log.e("SavingGoalRepo", "Network error", e)
            // Fallback to local data
            val localGoal = savingGoalDao.getSavingGoalById(id)?.toDomainModel()
            if (localGoal != null) {
                Result.success(localGoal)
            } else {
                Result.failure(Exception("Goal not found"))
            }
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun createSavingGoal(goal: SavingGoal): Result<SavingGoal> {
        return try {
            Log.d("SavingGoalRepo", "Creating goal locally: ${goal.title}")
            // Save locally first for immediate feedback
            savingGoalDao.insertSavingGoal(goal.toEntity())
            
            // Try to sync with server
            try {
                val response = api.createSavingGoal(goal)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d("SavingGoalRepo", "Server created goal with ID: ${body.id}")
                        // Only update if server returns different data
                        if (body.id != goal.id || body != goal) {
                            // Delete the local temporary goal and insert the server one
                            savingGoalDao.deleteSavingGoalById(goal.id)
                            savingGoalDao.insertSavingGoal(body.toEntity())
                            Log.d("SavingGoalRepo", "Replaced local goal with server goal")
                            return Result.success(body)
                        }
                    }
                } else {
                    Log.w("SavingGoalRepo", "Server goal creation failed: ${response.code()}")
                }
            } catch (e: Exception) {
                // Log but don't fail
                Log.w("SavingGoalRepo", "Failed to sync goal with server: ${e.message}")
            }
            
            Result.success(goal)
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Failed to create goal locally", e)
            Result.failure(Exception("Failed to create goal: ${e.message}", e))
        }
    }

    override suspend fun updateSavingGoal(goal: SavingGoal): Result<SavingGoal> {
        return try {
            // Update local cache first so edits work offline / immediately in UI
            savingGoalDao.updateSavingGoal(goal.toEntity())

            // Try to sync with server; if server responds with updated goal, overwrite local cache
            try {
                val response = api.updateSavingGoal(goal.id, goal)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Update local cache with server's canonical data
                        savingGoalDao.updateSavingGoal(body.toEntity())
                        return Result.success(body)
                    }
                } else {
                    Log.w("SavingGoalRepo", "Server update failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.w("SavingGoalRepo", "Failed to sync updated goal with server: ${e.message}")
            }

            // Return the locally-updated goal as success (network sync deferred)
            val local = savingGoalDao.getSavingGoalById(goal.id)?.toDomainModel()
            return if (local != null) Result.success(local) else Result.failure(Exception("Failed to update goal locally"))
        } catch (e: IOException) {
            Log.e("SavingGoalRepo", "Network error", e)
            Result.failure(Exception("Network error: ${e.message}", e))
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun deleteSavingGoal(goalId: String): Result<Unit> {
        return try {
            Log.d("SavingGoalRepo", "Deleting goal: $goalId")
            
            // Delete from local cache first (offline-first approach)
            savingGoalDao.deleteSavingGoalById(goalId)
            contributionDao.deleteAllContributionsForGoal(goalId)
            Log.d("SavingGoalRepo", "Goal deleted from local database")
            
            // Try to sync deletion with server
            try {
                val response = api.deleteSavingGoal(goalId)
                if (response.isSuccessful) {
                    Log.d("SavingGoalRepo", "Goal deletion synced with server")
                } else {
                    Log.w("SavingGoalRepo", "Server deletion failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.w("SavingGoalRepo", "Failed to sync goal deletion with server: ${e.message}")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Failed to delete goal locally", e)
            Result.failure(Exception("Failed to delete goal: ${e.message}", e))
        }
    }

    override fun observeSavingGoals(): Flow<List<SavingGoal>> {
        return savingGoalDao.observeAll().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun addContribution(contribution: SavingContribution): Result<SavingContribution> {
        return try {
            Log.d("SavingGoalRepo", "Adding contribution: ${contribution.amount} to goal ${contribution.goalId}")
            
            // First, save locally to ensure user sees immediate feedback
            contributionDao.insertContribution(contribution.toEntity())
            Log.d("SavingGoalRepo", "Contribution saved to local database")
            
            // Update goal progress locally and wait for completion
            val updateResult = updateGoalProgress(contribution.goalId)
            if (updateResult.isSuccess) {
                Log.d("SavingGoalRepo", "Goal progress updated successfully")
            } else {
                Log.w("SavingGoalRepo", "Failed to update goal progress: ${updateResult.exceptionOrNull()?.message}")
            }
            
            // Try to sync with server in the background
            try {
                val response = api.addContribution(contribution)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        // Update with server response if different
                        contributionDao.insertContribution(body.toEntity())
                        Log.d("SavingGoalRepo", "Contribution synced with server")
                    }
                } else {
                    Log.w("SavingGoalRepo", "Server sync failed: ${response.code()}")
                }
            } catch (e: Exception) {
                // Log network error but don't fail the operation
                Log.w("SavingGoalRepo", "Failed to sync contribution with server: ${e.message}")
            }
            
            Result.success(contribution)
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Failed to add contribution locally", e)
            Result.failure(Exception("Failed to add contribution: ${e.message}", e))
        }
    }

    override suspend fun getContributionsByGoalId(goalId: String): Result<List<SavingContribution>> {
        return try {
            val response = api.getContributionsByGoalId(goalId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Cache locally
                    val entities = body.map { it.toEntity() }
                    contributionDao.insertContributions(entities)
                    Result.success(body)
                } else {
                    // Fallback to local data
                    val localContributions = contributionDao.getContributionsByGoalIdOnce(goalId)
                        .map { it.toDomainModel() }
                    Result.success(localContributions)
                }
            } else {
                // Fallback to local data
                val localContributions = contributionDao.getContributionsByGoalIdOnce(goalId)
                    .map { it.toDomainModel() }
                Result.success(localContributions)
            }
        } catch (e: IOException) {
            Log.e("SavingGoalRepo", "Network error", e)
            // Fallback to local data
            val localContributions = contributionDao.getContributionsByGoalIdOnce(goalId)
                .map { it.toDomainModel() }
            Result.success(localContributions)
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Unknown error", e)
            Result.failure(Exception("Unknown error: ${e.message}", e))
        }
    }

    override suspend fun deleteContribution(contributionId: String): Result<Unit> {
        return try {
            Log.d("SavingGoalRepo", "Deleting contribution: $contributionId")
            
            // Delete from local cache first (offline-first approach)
            contributionDao.deleteContributionById(contributionId)
            Log.d("SavingGoalRepo", "Contribution deleted from local database")
            
            // Try to sync deletion with server
            try {
                val response = api.deleteContribution(contributionId)
                if (response.isSuccessful) {
                    Log.d("SavingGoalRepo", "Contribution deletion synced with server")
                } else {
                    Log.w("SavingGoalRepo", "Server deletion failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Log.w("SavingGoalRepo", "Failed to sync contribution deletion with server: ${e.message}")
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Failed to delete contribution locally", e)
            Result.failure(Exception("Failed to delete contribution: ${e.message}", e))
        }
    }

    override fun observeContributionsByGoalId(goalId: String): Flow<List<SavingContribution>> {
        return contributionDao.getContributionsByGoalId(goalId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun updateGoalProgress(goalId: String): Result<SavingGoal> {
        return try {
            Log.d("SavingGoalRepo", "Updating progress for goal: $goalId")
            
            // Get total contributions
            val totalContributions = contributionDao.getTotalContributionsForGoal(goalId) ?: 0.0
            Log.d("SavingGoalRepo", "Total contributions: $totalContributions")
            
            // Update current amount
            savingGoalDao.updateCurrentAmount(goalId, totalContributions)
            
            // Get updated goal
            val goal = savingGoalDao.getSavingGoalById(goalId)?.toDomainModel()
            if (goal != null) {
                Log.d("SavingGoalRepo", "Goal found: ${goal.title}, current: ${goal.currentAmount}, target: ${goal.goalAmount}")
                
                val isCompleted = totalContributions >= goal.goalAmount
                savingGoalDao.updateCompletionStatus(goalId, isCompleted)
                
                val updatedGoal = goal.copy(currentAmount = totalContributions, isCompleted = isCompleted)
                Log.d("SavingGoalRepo", "Updated goal progress successfully")
                Result.success(updatedGoal)
            } else {
                Log.e("SavingGoalRepo", "Goal not found: $goalId")
                Result.failure(Exception("Goal not found"))
            }
        } catch (e: Exception) {
            Log.e("SavingGoalRepo", "Failed to update progress for goal $goalId", e)
            Result.failure(Exception("Failed to update progress: ${e.message}", e))
        }
    }
}
