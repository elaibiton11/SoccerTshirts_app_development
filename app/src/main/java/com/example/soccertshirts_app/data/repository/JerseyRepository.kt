package com.example.soccertshirts_app.data.repository

import com.example.soccertshirts_app.data.local.dao.JerseyDao
import com.example.soccertshirts_app.data.local.entity.JerseyEntity
import com.example.soccertshirts_app.data.model.Comment
import com.example.soccertshirts_app.data.model.Jersey
import com.google.firebase.firestore.FieldValue
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
        
        // Fetch preview comments and count for each jersey
        val jerseysWithPreviews = jerseys.map { jersey ->
            val commentsSnapshot = db.collection("jerseys").document(jersey.id)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val allComments = commentsSnapshot.toObjects(Comment::class.java)
            jersey.copy(
                recentComments = allComments.take(3),
                commentsCount = allComments.size
            )
        }
        
        saveToLocal(jerseysWithPreviews)
        return jerseysWithPreviews
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
        val jersey = db.collection("jerseys").document(jerseyId).get().await().toObject(Jersey::class.java)
        return jersey?.let {
            val commentsSnapshot = db.collection("jerseys").document(it.id)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val allComments = commentsSnapshot.toObjects(Comment::class.java)
            it.copy(
                recentComments = allComments.take(3),
                commentsCount = allComments.size
            )
        }
    }

    suspend fun saveJersey(jersey: Jersey) {
        db.collection("jerseys").document(jersey.id).set(jersey).await()
        // Also update local cache for this specific item
        jerseyDao.insert(jersey.toEntity())
    }

    suspend fun toggleLike(jerseyId: String, userId: String) {
        val jerseyRef = db.collection("jerseys").document(jerseyId)
        val snapshot = jerseyRef.get().await()
        val jersey = snapshot.toObject(Jersey::class.java) ?: return

        val isLiked = jersey.likedBy.contains(userId)
        if (isLiked) {
            jerseyRef.update(
                "likedBy", FieldValue.arrayRemove(userId),
                "likesCount", FieldValue.increment(-1)
            ).await()
        } else {
            jerseyRef.update(
                "likedBy", FieldValue.arrayUnion(userId),
                "likesCount", FieldValue.increment(1)
            ).await()
        }

        // Update local cache
        val updatedJersey = getJerseyById(jerseyId)
        if (updatedJersey != null) {
            jerseyDao.insert(updatedJersey.toEntity())
        }
    }

    suspend fun getJerseysByOwner(ownerId: String): List<Jersey> {
        val snapshot = db.collection("jerseys")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()
        
        val jerseys = snapshot.toObjects(Jersey::class.java)
        
        return jerseys.map { jersey ->
            val commentsSnapshot = db.collection("jerseys").document(jersey.id)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val allComments = commentsSnapshot.toObjects(Comment::class.java)
            jersey.copy(
                recentComments = allComments.take(3),
                commentsCount = allComments.size
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun getUserData(uid: String): Map<String, Any>? {
        return try {
            val document = db.collection("users").document(uid).get().await()
            document.data
        } catch (e: Exception) {
            null
        }
    }

    // --- Comments Functions ---

    suspend fun getComments(jerseyId: String): List<Comment> {
        return db.collection("jerseys").document(jerseyId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .get()
            .await()
            .toObjects(Comment::class.java)
    }

    suspend fun addComment(jerseyId: String, comment: Comment) {
        val commentRef = db.collection("jerseys").document(jerseyId)
            .collection("comments").document()
        val commentWithId = comment.copy(id = commentRef.id)
        commentRef.set(commentWithId).await()
    }

    private fun JerseyEntity.toModel() = Jersey(
        id = id,
        title = title,
        team = team,
        year = year,
        price = price,
        country = country,
        description = description,
        imageUrl = imageUrl,
        ownerId = ownerId,
        ownerName = ownerName,
        ownerProfileImageUrl = ownerProfileImageUrl,
        createdAt = createdAt,
        likesCount = likesCount,
        likedBy = likedBy,
        commentsCount = commentsCount
    )

    private fun Jersey.toEntity() = JerseyEntity(
        id = id,
        title = title,
        team = team,
        year = year,
        price = price,
        country = country,
        description = description,
        imageUrl = imageUrl,
        ownerId = ownerId,
        ownerName = ownerName,
        ownerProfileImageUrl = ownerProfileImageUrl,
        createdAt = createdAt,
        likesCount = likesCount,
        likedBy = likedBy,
        commentsCount = commentsCount
    )
}
