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

    private fun JerseyEntity.toModel() = Jersey(
        id, title, team, year, price, description, imageUrl, ownerId, createdAt
    )

    private fun Jersey.toEntity() = JerseyEntity(
        id, title, team, year, price, description, imageUrl, ownerId, createdAt
    )
}