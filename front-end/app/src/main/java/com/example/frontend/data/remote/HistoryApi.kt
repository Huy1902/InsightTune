package com.example.frontend.data.remote

import com.example.frontend.data.models.song.HistoryRequest
import com.example.frontend.data.models.song.HistoryResponse
import com.example.frontend.data.models.song.SearchHistoryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface HistoryApi {
    @GET("history/search")
    suspend fun searchHistory() : List<SearchHistoryResponse>

    @GET("history")
    suspend fun getHistory() : List<HistoryResponse>

    @POST("history")
    suspend fun addHistory(@Body historyRequest: HistoryRequest) : HistoryResponse
}

