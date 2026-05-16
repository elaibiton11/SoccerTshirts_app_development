package com.example.soccertshirts_app.viewmodel

import androidx.lifecycle.*
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: JerseyRepository) : ViewModel() {

    val jerseys: LiveData<List<Jersey>> = repository.getLocalJerseys()

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun loadJerseys() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.fetchRemoteJerseys()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load jerseys: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteJersey(jersey: Jersey) {
        viewModelScope.launch {
            try {
                repository.deleteJersey(jersey.id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete jersey: ${e.message}"
            }
        }
    }

    fun toggleLike(jersey: Jersey) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.toggleLike(jersey.id, userId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update like: ${e.message}"
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

class HomeViewModelFactory(private val repository: JerseyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}