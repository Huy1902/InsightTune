package com.example.frontend.data.models.favorites

import java.util.Date


data class FavoriteResponse (
    val id: Int,
    val email: String,
    val songId: String,
    val created_at: Date
)

data class ImageResponse (
    val url: String
)