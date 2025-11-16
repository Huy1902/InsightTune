package com.example.frontend.domain.repositories

import com.example.frontend.data.models.favorites.FavoriteResponse
import com.example.frontend.data.models.home.GetTracksResponse

interface FavoriteRepository {
    suspend fun addFavorite(songId: String): FavoriteResponse
    suspend fun deleteFavorite(songId: String): String
    suspend fun getFavorites(): List<GetTracksResponse>
    suspend fun isFavorite(songId: String): Boolean
}


