package com.example.soccertshirts_app.viewmodel

import androidx.lifecycle.*
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.data.repository.JerseyRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: JerseyRepository) : ViewModel() {

    private val _jerseys = MutableLiveData<List<Jersey>>()
    val jerseys: LiveData<List<Jersey>> = _jerseys

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadJerseys() {
        viewModelScope.launch {
            try {
                // Load local cache first
                val localData = repository.getLocalJerseys()
                if (localData.isNotEmpty()) {
                    _jerseys.value = localData
                }

                // Fetch remote data
                val remoteData = repository.fetchRemoteJerseys()
                _jerseys.value = remoteData
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load jerseys: ${e.message}"
            }
        }
    }

    fun deleteJersey(jersey: Jersey) {
        viewModelScope.launch {
            try {
                repository.deleteJersey(jersey.id)
                // Refresh list after deletion
                loadJerseys()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete jersey: ${e.message}"
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