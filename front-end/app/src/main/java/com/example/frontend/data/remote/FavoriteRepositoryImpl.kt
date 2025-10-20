package com.example.frontend.data.remote

import com.example.frontend.data.models.song.FavoriteRequest
import com.example.frontend.data.models.song.FavoriteResponse
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.domain.repositories.FavoriteRepository

class FavoriteRepositoryImpl(private val api: FavoriteApi) : FavoriteRepository {
    override suspend fun addFavorite(songId: String): FavoriteResponse {
        val request = FavoriteRequest(songId)
        return api.addFavorite(request).body() ?: throw Exception("Failed to add favorite")
    }

    override suspend fun deleteFavorite(songId: String): String {
        val request = FavoriteRequest(songId)
        return api.deleteFavorite(request)
    }

    override suspend fun getFavorites(): List<GetTracksResponse> {
        val response = api.getFavorites()
        return response
    }
}