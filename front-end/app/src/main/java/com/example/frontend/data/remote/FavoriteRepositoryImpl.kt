package com.example.frontend.data.remote

import com.example.frontend.data.models.song.FavoriteResponse
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.domain.repositories.FavoriteRepository

class FavoriteRepositoryImpl : FavoriteRepository {
    override suspend fun addFavorite(songId: String): FavoriteResponse {
        return try {
            val response = trackApi.addFavorite(songId)
        }
    }

    override suspend fun deleteFavorite(songId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun getFavorites(): List<GetTracksResponse> {
        TODO("Not yet implemented")
    }
}