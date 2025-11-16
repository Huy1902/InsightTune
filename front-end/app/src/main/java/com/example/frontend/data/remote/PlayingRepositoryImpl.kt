package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.data.models.favorites.ImageResponse
import com.example.frontend.data.models.playingsong.PlayingRequest
import com.example.frontend.data.models.playingsong.PlayingResponse
import com.example.frontend.data.models.playingsong.UserState
import com.example.frontend.domain.repositories.PlayingRepository

class PlayingRepositoryImpl(
    private val playingApi: PlayingApi,
) : PlayingRepository {
    override suspend fun getUrlTrack(storageKey: String, coverImageKey: String?): PlayingResponse {
        try {
            val request = PlayingRequest(storageKey, coverImageKey)
            Log.d("PLAYING", "Sending playing request: $request")
            val response = playingApi.getPlaying(request)
            Log.d("PLAYING", "HTTP status: ${response.code()} - ${response.message()}")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.trackUrl.isNotEmpty()) {
                    return body
                } else {
                    Log.e("PLAYING", "Response successful but body is invalid.")
                }
            } else {
                Log.e("PLAYING", "HTTP Error: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("PLAYING", "An exception occurred", e)
        }
        return PlayingResponse("", "")

    }

    override suspend fun getImage(key: String): ImageResponse {
        val response = playingApi.getImage(key)
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty response")
            Log.d("PLAYING", "Got image: $body")
            return body
        }
        return response.body() ?: throw Exception("Empty response")
    }

    override suspend fun updateUserState(trackId: String, positionMs: Int): UserState {
        val request = UserState(trackId, positionMs)
        android.util.Log.d("PLAYING_STATE", "Request body: ${request.toString()}")
        val response = playingApi.updateUserState(request)
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty response")
            Log.d("PLAYING_STATE", "Updated user state: $body")
            return body
        } else {
            Log.e("PLAYING_STATE", "HTTP state Error: ${response.code()}")
            throw Exception("HTTP state Error: ${response.code()}")
        }
    }

    override suspend fun getUserState(): UserState {
        val response = playingApi.getUserState()
        if (response.isSuccessful) {
            val body = response.body() ?: throw Exception("Empty response")
            Log.d("PLAYING_STATE", "Got user state: $body")
            return body
        } else {
            Log.e("PLAYING_STATE", "HTTP Error: ${response.code()}")
            throw Exception("HTTP Error: ${response.code()}")
        }
    }
}