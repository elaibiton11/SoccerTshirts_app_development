package com.example.soccertshirts_app.viewmodel

import androidx.lifecycle.*
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.data.repository.JerseyRepository
import kotlinx.coroutines.launch

class JerseyDetailsViewModel(
    private val authRepository: AuthRepository,
    private val jerseyRepository: JerseyRepository
) : ViewModel() {

    private val _jersey = MutableLiveData<Jersey?>()
    val jersey: LiveData<Jersey?> = _jersey

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadJerseyDetails(jerseyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = jerseyRepository.getJerseyById(jerseyId)
                _jersey.value = result
            } catch (e: Exception) {
                _error.value = "Failed to load jersey details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleLike() {
        val currentJersey = _jersey.value ?: return
        val currentUser = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            try {
                jerseyRepository.toggleLike(currentJersey.id, currentUser.uid)
                // Reload to get updated likes count and list
                loadJerseyDetails(currentJersey.id)
            } catch (e: Exception) {
                _error.value = "Failed to update like: ${e.message}"
            }
        }
    }

    fun isLikedByUser(): Boolean {
        val currentUser = authRepository.getCurrentUser() ?: return false
        return _jersey.value?.likedBy?.contains(currentUser.uid) == true
    }

    fun clearError() {
        _error.value = null
    }
}

class JerseyDetailsViewModelFactory(
    private val authRepository: AuthRepository,
    private val jerseyRepository: JerseyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JerseyDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JerseyDetailsViewModel(authRepository, jerseyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}