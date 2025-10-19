package com.example.frontend.domain.repositories

import com.example.frontend.data.models.song.FavoriteResponse
import com.example.frontend.data.models.song.GetTracksResponse

interface FavoriteRepository {
    suspend fun addFavorite(songId: String): FavoriteResponse
    suspend fun deleteFavorite(songId: String): String
    suspend fun getFavorites(): List<GetTracksResponse>
}


