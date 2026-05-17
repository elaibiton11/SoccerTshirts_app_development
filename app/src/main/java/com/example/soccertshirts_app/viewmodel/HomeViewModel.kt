package com.example.soccertshirts_app.viewmodel

import androidx.lifecycle.*
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: JerseyRepository) : ViewModel() {

    private val _allJerseys = repository.getLocalJerseys()
    private val _searchQuery = MutableLiveData<String>("")

    val jerseys: LiveData<List<Jersey>> = MediatorLiveData<List<Jersey>>().apply {
        addSource(_allJerseys) { jerseys ->
            value = filterJerseys(jerseys, _searchQuery.value ?: "")
        }
        addSource(_searchQuery) { query ->
            value = filterJerseys(_allJerseys.value ?: emptyList(), query)
        }
    }

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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private fun filterJerseys(list: List<Jersey>, query: String): List<Jersey> {
        if (query.isBlank()) return list
        val lowerQuery = query.lowercase()
        return list.filter {
            it.title.lowercase().contains(lowerQuery) ||
            it.team.lowercase().contains(lowerQuery) ||
            it.description.lowercase().contains(lowerQuery)
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