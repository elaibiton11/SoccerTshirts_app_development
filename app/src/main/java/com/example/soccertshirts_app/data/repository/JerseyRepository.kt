package com.example.soccertshirts_app.data.repository

import com.example.soccertshirts_app.data.local.dao.JerseyDao
import com.example.soccertshirts_app.data.local.entity.JerseyEntity
import com.example.soccertshirts_app.data.model.Jersey
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class JerseyRepository(private val jerseyDao: JerseyDao) {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getLocalJerseys(): List<Jersey> {
        return jerseyDao.getAllJerseys().map { it.toModel() }
    }

    suspend fun fetchRemoteJerseys(): List<Jersey> {
        val snapshot = db.collection("jerseys")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        
        val jerseys = snapshot.toObjects(Jersey::class.java)
        saveToLocal(jerseys)
        return jerseys
    }

    private suspend fun saveToLocal(jerseys: List<Jersey>) {
        val entities = jerseys.map { it.toEntity() }
        jerseyDao.deleteAll()
        jerseyDao.insertAll(entities)
    }

    suspend fun deleteJersey(jerseyId: String) {
        // Delete from Firestore
        db.collection("jerseys").document(jerseyId).delete().await()
        // Delete from Room
        jerseyDao.deleteById(jerseyId)
    }

    suspend fun getJerseyById(jerseyId: String): Jersey? {
        return db.collection("jerseys").document(jerseyId).get().await().toObject(Jersey::class.java)
    }

    suspend fun saveJersey(jersey: Jersey) {
        db.collection("jerseys").document(jersey.id).set(jersey).await()
        // Also update local cache for this specific item
        jerseyDao.insert(jersey.toEntity())
    }

    suspend fun getJerseysByOwner(ownerId: String): List<Jersey> {
        val snapshot = db.collection("jerseys")
            .whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.toObjects(Jersey::class.java)
    }

    suspend fun getUserData(uid: String): Map<String, Any>? {
        return try {
            val document = db.collection("users").document(uid).get().await()
            document.data
        } catch (e: Exception) {
            null
        }
    }

    private fun JerseyEntity.toModel() = Jersey(
        id = id,
        title = title,
        team = team,
        year = year,
        price = price,
        description = description,
        imageUrl = imageUrl,
        ownerId = ownerId,
        ownerName = ownerName,
        ownerProfileImageUrl = ownerProfileImageUrl,
        createdAt = createdAt
    )

    private fun Jersey.toEntity() = JerseyEntity(
        id = id,
        title = title,
        team = team,
        year = year,
        price = price,
        description = description,
        imageUrl = imageUrl,
        ownerId = ownerId,
        ownerName = ownerName,
        ownerProfileImageUrl = ownerProfileImageUrl,
        createdAt = createdAt
    )
}