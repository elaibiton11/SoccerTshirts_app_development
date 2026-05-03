package com.example.soccertshirts_app.data.model

data class Jersey(
    val id: String = "",
    val title: String = "",
    val team: String = "",
    val year: Int = 0,
    val price: Double = 0.0,
    val description: String = "",
    val imageUrl: String = "",
    val ownerId: String = ""
)