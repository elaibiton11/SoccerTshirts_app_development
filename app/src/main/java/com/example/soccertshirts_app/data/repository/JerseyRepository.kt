package com.example.soccertshirts_app.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
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

    val allJerseys: LiveData<List<Jersey>> = jerseyDao.getAllJerseys().map { entities ->
        entities.map { it.toModel() }
    }

    fun getLocalJerseys(): LiveData<List<Jersey>> = allJerseys

    suspend fun fetchRemoteJerseys(): List<Jersey> {
        val snapshot = db.collection("jerseys")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()

        val jerseys = snapshot.toObjects(Jersey::class.java)
        val enrichedJerseys = jerseys.map { jersey ->
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

        saveToLocal(enrichedJerseys)
        return enrichedJerseys
    }

    private suspend fun saveToLocal(jerseys: List<Jersey>) {
        val entities = jerseys.map { it.toEntity() }
        jerseyDao.deleteAll()
        jerseyDao.insertAll(entities)
    }

    suspend fun deleteJersey(jerseyId: String) {
        db.collection("jerseys").document(jerseyId).delete().await()
        jerseyDao.deleteById(jerseyId)
    }

    suspend fun getJerseyById(jerseyId: String): Jersey? {
        val doc = db.collection("jerseys").document(jerseyId).get().await()
        val jersey = doc.toObject(Jersey::class.java)
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

        val updated = getJerseyById(jerseyId)
        if (updated != null) {
            jerseyDao.insert(updated.toEntity())
        }
    }

    suspend fun getUserData(uid: String): Map<String, Any>? {
        return db.collection("users").document(uid).get().await().data
    }

    suspend fun getComments(jerseyId: String): List<Comment> {
        val snapshot = db.collection("jerseys").document(jerseyId)
            .collection("comments")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .await()
        return snapshot.toObjects(Comment::class.java)
    }

    suspend fun addComment(jerseyId: String, comment: Comment) {
        db.collection("jerseys").document(jerseyId)
            .collection("comments")
            .add(comment)
            .await()
    }

    suspend fun getJerseysByOwner(ownerId: String): List<Jersey> {
        val snapshot = db.collection("jerseys")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .await()
        return snapshot.toObjects(Jersey::class.java).sortedByDescending { it.createdAt }
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
        createdAt = createdAt,
        likesCount = likesCount,
        likedBy = likedBy,
        recentComments = recentComments,
        commentsCount = commentsCount
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
        createdAt = createdAt,
        likesCount = likesCount,
        likedBy = likedBy,
        commentsCount = commentsCount,
        recentComments = recentComments
    )
}
