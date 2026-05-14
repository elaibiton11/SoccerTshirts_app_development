package com.example.soccertshirts_app.viewmodel

import androidx.lifecycle.*
import com.example.soccertshirts_app.data.model.Comment
import com.example.soccertshirts_app.data.model.UserProfile
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.data.repository.JerseyRepository
import kotlinx.coroutines.launch

class CommentsViewModel(
    private val authRepository: AuthRepository,
    private val jerseyRepository: JerseyRepository
) : ViewModel() {

    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _commentAdded = MutableLiveData<Boolean>()
    val commentAdded: LiveData<Boolean> = _commentAdded

    fun loadComments(jerseyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val commentsList = jerseyRepository.getComments(jerseyId)
                _comments.value = commentsList
            } catch (e: Exception) {
                _error.value = "Failed to load comments: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addComment(jerseyId: String, text: String) {
        if (text.isBlank()) return

        val currentUser = authRepository.getCurrentUser() ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userProfile = authRepository.getUserProfile(currentUser.uid)
                
                val newComment = Comment(
                    jerseyId = jerseyId,
                    userId = currentUser.uid,
                    username = userProfile?.username ?: "Anonymous",
                    userProfileImageUrl = userProfile?.profileImageUrl ?: "",
                    text = text,
                    createdAt = System.currentTimeMillis()
                )
                
                jerseyRepository.addComment(jerseyId, newComment)
                _commentAdded.value = true
                
                // Reload comments
                loadComments(jerseyId)
            } catch (e: Exception) {
                _error.value = "Failed to add comment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetCommentAdded() {
        _commentAdded.value = false
    }

    fun clearError() {
        _error.value = null
    }
}

class CommentsViewModelFactory(
    private val authRepository: AuthRepository,
    private val jerseyRepository: JerseyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CommentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CommentsViewModel(authRepository, jerseyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}