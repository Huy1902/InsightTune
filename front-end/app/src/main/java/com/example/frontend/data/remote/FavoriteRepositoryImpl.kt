package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.data.models.favorites.FavoriteRequest
import com.example.frontend.data.models.favorites.FavoriteResponse
import com.example.frontend.data.models.home.GetTracksResponse
import com.example.frontend.domain.repositories.FavoriteRepository

class FavoriteRepositoryImpl(private val api: FavoriteApi) : FavoriteRepository {
    override suspend fun addFavorite(songId: String): FavoriteResponse {
        val request = FavoriteRequest(songId)
        return api.addFavorite(request).body() ?: throw Exception("Failed to add favorite")
    }

    override suspend fun deleteFavorite(songId: String): String {
        val request = FavoriteRequest(songId)
        val response = api.deleteFavorite(request)
        val message = response.body()?.string() ?: throw Exception("Failed to delete favorite")
        Log.d("FavoriteRepo", "Delete response: $message")
        return message
    }

    override suspend fun getFavorites(): List<GetTracksResponse> {
        val response = api.getFavorites()
        return response
    }

    override suspend fun isFavorite(songId: String): Boolean {
        return api.isFavorite(songId)
    }
}