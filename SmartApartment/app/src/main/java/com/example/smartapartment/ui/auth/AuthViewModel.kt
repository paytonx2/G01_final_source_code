package com.example.smartapartment.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartapartment.models.LoginResponse
import com.example.smartapartment.models.RegisterResponse
import com.example.smartapartment.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _loginState = MutableStateFlow<UiState<LoginResponse>>(UiState.Loading(false))
    val loginState: StateFlow<UiState<LoginResponse>> = _loginState

    private val _registerState = MutableStateFlow<UiState<RegisterResponse>>(UiState.Loading(false))
    val registerState: StateFlow<UiState<RegisterResponse>> = _registerState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading(true)
            try {
                val response = repository.login(email, password)
                if (response.isSuccessful && response.body() != null) {
                    _loginState.value = UiState.Success(response.body()!!)
                } else {
                    _loginState.value = UiState.Error("Login failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _loginState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun register(name: String, email: String, password: String, phone: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading(true)
            try {
                val response = repository.register(name, email, password, phone)
                if (response.isSuccessful && response.body() != null) {
                    _registerState.value = UiState.Success(response.body()!!)
                } else {
                    _registerState.value = UiState.Error("Registration failed: ${response.message()}")
                }
            } catch (e: Exception) {
                _registerState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}

sealed class UiState<out T> {
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    data class Loading(val isLoading: Boolean) : UiState<Nothing>()
}
