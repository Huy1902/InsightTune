package com.example.frontend.data.remote

import com.example.frontend.data.models.song.GetTracksResponse
import okhttp3.Response
import retrofit2.http.GET

interface TrackApi {
    @GET("tracks")
    suspend fun getTracks(): List<GetTracksResponse>
}