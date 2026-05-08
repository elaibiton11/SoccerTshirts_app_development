package com.example.soccertshirts_app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()

    suspend fun login(email: String, password: String): Boolean {
        return auth.signInWithEmailAndPassword(email, password).await() != null
    }

    suspend fun register(email: String, password: String): Boolean {
        return auth.createUserWithEmailAndPassword(email, password).await() != null
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