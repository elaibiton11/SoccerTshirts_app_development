package com.example.soccertshirts_app.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.example.soccertshirts_app.data.model.Jersey
import com.example.soccertshirts_app.data.repository.JerseyRepository
import com.example.soccertshirts_app.data.services.CloudinaryModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class AddEditJerseyViewModel(private val repository: JerseyRepository) : ViewModel() {

    private val _jersey = MutableLiveData<Jersey?>()
    val jersey: LiveData<Jersey?> = _jersey

    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean> = _isSaved

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var existingJersey: Jersey? = null

    fun loadJersey(jerseyId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val jersey = repository.getJerseyById(jerseyId)
                existingJersey = jersey
                _jersey.value = jersey
            } catch (e: Exception) {
                _error.value = "Failed to load jersey: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveJersey(
        title: String,
        team: String,
        year: Int?,
        price: Double?,
        description: String,
        selectedImageUri: Uri?
    ) {
        val ownerId = FirebaseAuth.getInstance().currentUser?.uid
        if (ownerId == null) {
            _error.value = "User not logged in"
            return
        }

        if (!validate(title, team, year, price, description, selectedImageUri)) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                var imageUrl = existingJersey?.imageUrl ?: ""
                
                // If a new image was selected, upload it
                if (selectedImageUri != null) {
                    val publicId = "jersey_${UUID.randomUUID()}"
                    imageUrl = uploadImage(selectedImageUri, publicId) ?: throw Exception("Image upload failed")
                }

                val jerseyId = existingJersey?.id ?: UUID.randomUUID().toString()
                val createdAt = existingJersey?.createdAt ?: System.currentTimeMillis()

                val newJersey = Jersey(
                    id = jerseyId,
                    title = title,
                    team = team,
                    year = year ?: 0,
                    price = price ?: 0.0,
                    description = description,
                    imageUrl = imageUrl,
                    ownerId = ownerId,
                    createdAt = createdAt
                )

                repository.saveJersey(newJersey)
                _isSaved.value = true
            } catch (e: Exception) {
                _error.value = "Failed to save jersey: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validate(
        title: String,
        team: String,
        year: Int?,
        price: Double?,
        description: String,
        selectedImageUri: Uri?
    ): Boolean {
        if (title.isBlank()) { _error.value = "Title is required"; return false }
        if (team.isBlank()) { _error.value = "Team is required"; return false }
        if (year == null) { _error.value = "Valid year is required"; return false }
        if (price == null || price <= 0) { _error.value = "Valid price is required"; return false }
        if (description.isBlank()) { _error.value = "Description is required"; return false }
        
        if (selectedImageUri == null && (existingJersey == null || existingJersey?.imageUrl.isNullOrEmpty())) {
            _error.value = "Please select an image"
            return false
        }
        return true
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
}

class AddEditJerseyViewModelFactory(private val repository: JerseyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddEditJerseyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddEditJerseyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}