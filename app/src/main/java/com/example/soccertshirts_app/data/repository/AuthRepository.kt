package com.example.soccertshirts_app.data.repository

import com.example.soccertshirts_app.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun login(email: String, password: String): Boolean {
        return auth.signInWithEmailAndPassword(email, password).await() != null
    }

    suspend fun register(email: String, username: String, password: String): Boolean {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user
        if (user != null) {
            val profile = UserProfile(
                uid = user.uid,
                email = email,
                username = username,
                profileImageUrl = ""
            )
            db.collection("users").document(user.uid).set(profile).await()
            return true
        }
        return false
    }

    suspend fun getUserProfile(uid: String): UserProfile? {
        return db.collection("users").document(uid).get().await().toObject(UserProfile::class.java)
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        db.collection("users").document(profile.uid).set(profile).await()
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}