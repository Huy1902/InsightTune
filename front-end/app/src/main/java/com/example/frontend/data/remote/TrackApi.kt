package com.example.frontend.data.remote

import com.example.frontend.data.models.home.GetTracksResponse
import com.example.frontend.data.models.playingsong.NextTracksResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TrackApi {
    @GET("tracks")
    suspend fun getTracks(): List<GetTracksResponse>

    @GET("tracks/search")
    suspend fun searchTracks(
        @Query("keyword") keyword: String
    ): List<GetTracksResponse>

    @GET("tracks/next/{currentTrackId}")
    suspend fun nextTracks(
        @Path("currentTrackId") currentTrackId: String
    ): List<NextTracksResponse>

    @POST("tracks/by-ids")
    suspend fun getTracksById(@Body trackIds: List<String>): List<GetTracksResponse>

    @GET("recommend?num_item=5")
    suspend fun getRecommendTracks(): List<GetTracksResponse>
}