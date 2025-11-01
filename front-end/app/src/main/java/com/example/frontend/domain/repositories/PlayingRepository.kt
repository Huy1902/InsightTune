package com.example.frontend.domain.repositories

import com.example.frontend.data.models.favorites.ImageResponse
import com.example.frontend.data.models.playingsong.PlayingResponse

interface PlayingRepository {
    suspend fun getUrlTrack(storageKey: String, coverImageKey: String?): PlayingResponse

    suspend fun getImage(key: String) : ImageResponse
}