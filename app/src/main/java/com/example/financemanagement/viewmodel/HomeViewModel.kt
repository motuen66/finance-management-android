package com.example.financemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.repository.BudgetRepository
import com.example.financemanagement.data.repository.SavingGoalRepository
import com.example.financemanagement.data.repository.TransactionRepository
import com.example.financemanagement.domain.model.Budget
import com.example.financemanagement.domain.model.SavingGoal
import com.example.financemanagement.domain.model.Transaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
//    private val budgetRepository: BudgetRepository,
): ViewModel() {
//    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
//    val budget: StateFlow<List<Budget>> = _budgets

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

//    fun fetchBudgets() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _error.value = null
//
//            // TODO: remove hard code by real userId
////            val result = budgetRepository.getBudgetsByUserId("6904cf1320173db06b2641b8")
//
//            if (result.isSuccess) {
//                _budgets.value = result.getOrThrow()
//            } else {
//                _error.value = result.exceptionOrNull()?.message ?: "Unknown error"
//            }
//
//            _isLoading.value = false
//        }
//    }
}