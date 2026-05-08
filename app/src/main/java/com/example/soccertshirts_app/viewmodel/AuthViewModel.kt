package com.example.soccertshirts_app.viewmodel

import androidx.lifecycle.*
import com.example.soccertshirts_app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _authSuccess = MutableLiveData<Boolean>()
    val authSuccess: LiveData<Boolean> = _authSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    val currentUserId: String?
        get() = repository.getCurrentUser()?.uid

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val success = repository.login(email, password)
                _authSuccess.value = success
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun register(email: String, username: String, password: String) {
        viewModelScope.launch {
            try {
                if (username.isBlank()) {
                    _errorMessage.value = "Username is required"
                    return@launch
                }
                val success = repository.register(email, username, password)
                _authSuccess.value = success
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun checkUserLoggedIn() {
        _isLoggedIn.value = repository.isUserLoggedIn()
    }

    fun logout() {
        repository.logout()
        _isLoggedIn.value = false
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}