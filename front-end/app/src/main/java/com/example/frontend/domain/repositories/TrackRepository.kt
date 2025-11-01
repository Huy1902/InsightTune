package com.example.frontend.domain.repositories

import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.models.song.NextTracksResponse
import com.example.frontend.data.remote.TrackApi
import retrofit2.Response

interface TrackRepository {
    suspend fun getTracks(): List<GetTracksResponse>
    suspend fun searchTracks(keyword: String): List<GetTracksResponse>

    suspend fun nextTracks(currentTrackId: String): List<NextTracksResponse>

    suspend fun getTrackById(trackId: String): GetTracksResponse

}