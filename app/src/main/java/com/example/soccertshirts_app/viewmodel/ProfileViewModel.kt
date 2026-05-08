package com.example.soccertshirts_app.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.data.model.UserProfile
import com.example.soccertshirts_app.data.repository.AuthRepository
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.example.soccertshirts_app.data.services.CloudinaryModel
import kotlinx.coroutines.launch
import java.util.*

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val jerseyRepository: JerseyRepository
) : ViewModel() {

    private val _profile = MutableLiveData<UserProfile?>()
    val profile: LiveData<UserProfile?> = _profile

    private val _userJerseys = MutableLiveData<List<Jersey>>()
    val userJerseys: LiveData<List<Jersey>> = _userJerseys

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isUpdated = MutableLiveData<Boolean>()
    val isUpdated: LiveData<Boolean> = _isUpdated

    fun loadProfile() {
        val uid = authRepository.getCurrentUser()?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userProfile = authRepository.getUserProfile(uid)
                _profile.value = userProfile
                
                // Also load jerseys
                val jerseys = jerseyRepository.getJerseysByOwner(uid)
                _userJerseys.value = jerseys
            } catch (e: Exception) {
                _error.value = "Failed to load profile data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(username: String, selectedImageUri: Uri?) {
        val currentProfile = _profile.value ?: return
        if (username.isBlank()) {
            _error.value = "Username cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                var imageUrl = currentProfile.profileImageUrl
                
                if (selectedImageUri != null) {
                    val publicId = "profile_${currentProfile.uid}_${UUID.randomUUID()}"
                    imageUrl = uploadImage(selectedImageUri, publicId) ?: throw Exception("Image upload failed")
                }

                val updatedProfile = currentProfile.copy(
                    username = username,
                    profileImageUrl = imageUrl
                )

                authRepository.updateUserProfile(updatedProfile)
                _profile.value = updatedProfile
                _isUpdated.value = true
            } catch (e: Exception) {
                _error.value = "Failed to update profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun uploadImage(uri: Uri, publicId: String): String? {
        return kotlin.coroutines.suspendCoroutine { continuation ->
            CloudinaryModel.uploadImage(uri, publicId) { url ->
                continuation.resumeWith(Result.success(url))
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun resetUpdated() {
        _isUpdated.value = false
    }
}

class ProfileViewModelFactory(
    private val authRepository: AuthRepository,
    private val jerseyRepository: JerseyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(authRepository, jerseyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}