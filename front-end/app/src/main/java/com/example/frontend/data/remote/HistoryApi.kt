package com.example.frontend.data.remote

import com.example.frontend.data.models.song.SearchHistoryResponse
import retrofit2.Response
import retrofit2.http.GET

interface HistoryApi {
    @GET("history/search")
    suspend fun searchHistory() : List<SearchHistoryResponse>
}