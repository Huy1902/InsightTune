package com.example.frontend.domain.repositories

import com.example.frontend.data.models.favorites.ImageResponse
import com.example.frontend.data.models.login.GoogleRequest
import com.example.frontend.data.models.playingsong.PlayingResponse
import com.example.frontend.data.models.playingsong.UserState

interface PlayingRepository {
    suspend fun getUrlTrack(storageKey: String, coverImageKey: String?): PlayingResponse

    suspend fun getImage(key: String) : ImageResponse

    suspend fun updateUserState(trackId: String, positionMs: Int) : UserState

    suspend fun getUserState() : UserState
}