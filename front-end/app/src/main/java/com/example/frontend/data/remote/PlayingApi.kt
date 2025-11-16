package com.example.frontend.data.remote

import com.example.frontend.data.models.favorites.ImageResponse
import com.example.frontend.data.models.playingsong.PlayingRequest
import com.example.frontend.data.models.playingsong.PlayingResponse
import com.example.frontend.data.models.playingsong.UserState
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PlayingApi {

    @POST("play")
    suspend fun getPlaying(
        @Body request: PlayingRequest
    ): Response<PlayingResponse>

    @GET("play/url")
    suspend fun getImage(
        @Query("key") key: String
    ): Response<ImageResponse>

    @POST("play/user_state")
    suspend fun updateUserState(
        @Body request: UserState
    ): Response<UserState>

    @GET("play/user_state")
    suspend fun getUserState(
    ): Response<UserState>
}