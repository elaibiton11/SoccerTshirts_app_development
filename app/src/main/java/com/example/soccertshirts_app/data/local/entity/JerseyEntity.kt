package com.example.soccertshirts_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "jerseys")
data class JerseyEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val team: String,
    val year: Int,
    val price: Double,
    val description: String,
    val imageUrl: String,
    val ownerId: String,
    val ownerName: String,
    val ownerProfileImageUrl: String,
    val createdAt: Long,
    val likesCount: Int,
    val likedBy: List<String>
)