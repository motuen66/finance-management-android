package com.example.financemanagement.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financemanagement.data.remote.models.LoginRequest
import com.example.financemanagement.data.remote.models.LoginResponse
import com.example.financemanagement.data.remote.models.RegisterRequest
import com.example.financemanagement.data.remote.models.RegisterResponse
import com.example.financemanagement.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class LoginSuccess(val response: LoginResponse) : AuthUiState
    data class RegisterSuccess(val response: RegisterResponse) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repo.login(LoginRequest(email = email, password = password))
            if (result.isSuccess) {
                _uiState.value = AuthUiState.LoginSuccess(result.getOrThrow())
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repo.register(RegisterRequest(name = name, email = email, password = password))
            if (result.isSuccess) {
                _uiState.value = AuthUiState.RegisterSuccess(result.getOrThrow())
            } else {
                _uiState.value = AuthUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.logout()
            _uiState.value = AuthUiState.Idle
        }
    }
}
