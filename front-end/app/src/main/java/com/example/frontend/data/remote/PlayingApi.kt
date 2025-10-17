package com.example.frontend.data.remote

import com.example.frontend.data.models.song.PlayingRequest
import com.example.frontend.data.models.song.PlayingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PlayingApi {

    @POST("play")
    suspend fun getPlaying(
        @Body request: PlayingRequest
    ) : Response<PlayingResponse>

}