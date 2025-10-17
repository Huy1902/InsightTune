package com.example.frontend.data.remote

import android.util.Log
import androidx.compose.ui.text.resolveDefaults
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.song.PlayingRequest
import com.example.frontend.data.models.song.PlayingResponse
import com.example.frontend.domain.repositories.PlayingRepository

class PlayingRepositoryImpl(
    private val playingApi: PlayingApi,
) : PlayingRepository {
    override suspend fun getUrlTrack(storageKey: String, coverImageKey: String): PlayingResponse {
        val request = PlayingRequest(storageKey, coverImageKey)
        Log.d("PLAYING", "Sending playing request: $request")
        val response = playingApi.getPlaying(request)
        Log.d("PLAYING", "HTTP status: ${response.code()} - ${response.message()}")

        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty response")
            Log.d("PLAYING", "Response body: $body")
            if (body.trackUrl.isEmpty()) {
                throw Exception("Invalid response")
            } else {
                Log.d("PLAYING", "Track URL: ${body.trackUrl}")
                Log.d("PLAYING", "Cover Image URL: ${body.coverImageUrl}")
            }
            return body
        }
        else {
            throw Exception("HTTP ${response.code()}: ${response.message()}")
        }
    }
}