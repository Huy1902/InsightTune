package com.example.frontend.data.remote

import com.example.frontend.data.models.song.ImageResponse
import com.example.frontend.data.models.song.PlayingRequest
import com.example.frontend.data.models.song.PlayingResponse
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

    @GET("url")
    suspend fun getImage(
        @Query("key") key: String
    ): Response<ImageResponse>
}