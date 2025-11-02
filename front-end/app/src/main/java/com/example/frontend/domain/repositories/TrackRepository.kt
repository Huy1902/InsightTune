package com.example.frontend.domain.repositories

import com.example.frontend.data.models.home.GetTracksResponse
import com.example.frontend.data.models.playingsong.NextTracksResponse

interface TrackRepository {
    suspend fun getTracks(): List<GetTracksResponse>
    suspend fun searchTracks(keyword: String): List<GetTracksResponse>

    suspend fun nextTracks(currentTrackId: String): List<NextTracksResponse>

    suspend fun getTrackById(trackId: String): GetTracksResponse

}