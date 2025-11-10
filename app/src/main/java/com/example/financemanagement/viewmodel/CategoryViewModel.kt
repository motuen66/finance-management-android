package com.example.financemanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.local.TokenManager
import com.example.financemanagement.data.repository.CategoryRepository
import com.example.financemanagement.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CategoryUiState {
    object Loading : CategoryUiState()
    data class Success(val categories: List<Category>) : CategoryUiState()
    data class Error(val message: String) : CategoryUiState()
}

sealed class CategoryActionState {
    object Idle : CategoryActionState()
    object Loading : CategoryActionState()
    object Success : CategoryActionState()
    data class Error(val message: String) : CategoryActionState()
}

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    
    private val _uiState = MutableStateFlow<CategoryUiState>(CategoryUiState.Loading)
    val uiState: StateFlow<CategoryUiState> = _uiState

    private val _actionState = MutableStateFlow<CategoryActionState>(CategoryActionState.Idle)
    val actionState: StateFlow<CategoryActionState> = _actionState

    private val _filterType = MutableStateFlow<String?>(null) // null = All, "Income", "Expense"
    val filterType: StateFlow<String?> = _filterType

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            _uiState.value = CategoryUiState.Loading

            val result = categoryRepository.getCategories()

            if (result.isSuccess) {
                _allCategories.value = result.getOrThrow()
                applyFilter()
            } else {
                _uiState.value = CategoryUiState.Error(
                    result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }

    fun setFilter(type: String?) {
        _filterType.value = type
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = when (_filterType.value) {
            null -> _allCategories.value // All
            else -> _allCategories.value.filter { it.type.equals(_filterType.value, ignoreCase = true) }
        }
        _uiState.value = CategoryUiState.Success(filtered)
    }

    fun createCategory(name: String, type: String) {
        viewModelScope.launch {
            _actionState.value = CategoryActionState.Loading

            val result = categoryRepository.createCategory(name, type)

            if (result.isSuccess) {
                _actionState.value = CategoryActionState.Success
                fetchCategories() // Refresh list
            } else {
                _actionState.value = CategoryActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to create category"
                )
            }
        }
    }

    fun updateCategory(id: String, name: String, type: String) {
        viewModelScope.launch {
            _actionState.value = CategoryActionState.Loading


            val result = categoryRepository.updateCategory(id, name, type)

            if (result.isSuccess) {
                _actionState.value = CategoryActionState.Success
                fetchCategories()
            } else {
                _actionState.value = CategoryActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to update category"
                )
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            _actionState.value = CategoryActionState.Loading

            val result = categoryRepository.deleteCategory(id)

            if (result.isSuccess) {
                _actionState.value = CategoryActionState.Success
                fetchCategories() // Refresh list
            } else {
                _actionState.value = CategoryActionState.Error(
                    result.exceptionOrNull()?.message ?: "Failed to delete category"
                )
            }
        }
    }

    fun resetActionState() {
        _actionState.value = CategoryActionState.Idle
    }
}
