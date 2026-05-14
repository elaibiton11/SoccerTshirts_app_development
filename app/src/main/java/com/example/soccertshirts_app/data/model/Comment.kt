package com.example.soccertshirts_app.data.model

data class Comment(
    val id: String = "",
    val jerseyId: String = "",
    val userId: String = "",
    val username: String = "",
    val userProfileImageUrl: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis()
)