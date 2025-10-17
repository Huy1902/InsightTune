package com.example.frontend.domain.repositories

import com.example.frontend.data.models.song.PlayingResponse

interface PlayingRepository {
    suspend fun getUrlTrack(storageKey: String, coverImageKey: String?): PlayingResponse
}