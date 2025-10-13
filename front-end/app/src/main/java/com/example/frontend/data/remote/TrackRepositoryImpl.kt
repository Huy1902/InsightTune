package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.domain.repositories.TrackRepository

class TrackRepositoryImpl(private val trackApi: TrackApi) : TrackRepository {

    companion object {
        private const val TAG = "TrackRepositoryImpl"
    }

    override suspend fun getTracks(): List<GetTracksResponse> {
        return try {
            Log.d(TAG, "Call API getTracks()...")
            val response = trackApi.getTracks()
            Log.d(TAG, "API response: ${response.size} songs")

            if (response.isNotEmpty()) {
                val first = response.first()
                Log.d(TAG, "First song: title=${first.title}, cover=${first.coverImageKey}, storageKey=${first.storageKey}")
            } else {
                Log.w(TAG, "Empty response from API")
            }

            response
        } catch (e: Exception) {
            Log.e(TAG, "Error when calling API: ${e.message}", e)
            emptyList()
        }
    }
}
