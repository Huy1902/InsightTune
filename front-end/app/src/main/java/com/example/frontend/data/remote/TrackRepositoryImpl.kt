package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.data.models.home.GetTracksResponse
import com.example.frontend.data.models.playingsong.NextTracksResponse
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
                Log.d(TAG, "First song: title=${first.title}, cover=${first.coverImageKey}, storageKey=${first.storageKey}}")
            } else {
                Log.w(TAG, "Empty response from API")
            }
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error when calling API: ${e.message}", e)
            emptyList()
        }
    }

    override suspend fun searchTracks(keyword: String): List<GetTracksResponse> {
        return try {
            Log.d(TAG, "Call API searchTracks()...")
            val response = trackApi.searchTracks(keyword)
            Log.d(TAG, "API response: ${response.size} songs")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error when calling API: ${e.message}", e)
            emptyList()
        }
        Log.d(TAG, "End of searchTracks()")
    }

    override suspend fun nextTracks(currentTrackId: String): List<NextTracksResponse> {
        return try {
            Log.d(TAG, "call API nextTracks()...")
            val response = trackApi.nextTracks(currentTrackId)
            Log.d(TAG, "API response: ${response} songs")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Error when calling API: ${e.message}", e)
            emptyList()
        }
        Log.d(TAG, "End of nextTracks()")
    }

    override suspend fun getTrackById(trackId: String): GetTracksResponse {
        return try {
            val response = trackApi.getTracksById(listOf(trackId))
            Log.d(TAG, "API response: $response")
            return response.firstOrNull() ?: GetTracksResponse("", "", emptyList(), "", "", 0L, "")
        } catch (e: Exception) {
            Log.e(TAG, "Error when calling API: ${e.message}", e)
            GetTracksResponse("", "", emptyList(), "", "", 0L, "")
        }
    }
}
