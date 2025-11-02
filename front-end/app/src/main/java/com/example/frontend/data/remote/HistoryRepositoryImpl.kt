package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.history.HistoryRequest
import com.example.frontend.data.models.history.HistoryResponse
import com.example.frontend.data.models.search.SearchHistoryResponse
import com.example.frontend.domain.repositories.HistoryRepository
import java.util.Date
import kotlin.collections.emptyList

class HistoryRepositoryImpl(
    private val historyApi: HistoryApi,
    private val prefs: AppPreferences
) : HistoryRepository {
    override suspend fun getSearchHistory(): List<SearchHistoryResponse> {
        return try {
            val response = historyApi.searchHistory()
            response
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getHistory(): List<HistoryResponse> {
        return try {
            Log.d("HistoryRepositoryImpl", "getHistory() called")
            val response = historyApi.getHistory()
            Log.d("HistoryRepositoryImpl", "getHistory() response: $response")
            response
        } catch (e: Exception) {
            Log.e("HistoryRepositoryImpl", "getHistory() error: ${e.message}", e)
            emptyList()
        }
    }


    override suspend fun addHistory(trackId: String, storageKey: String): HistoryResponse {
        return try {
            Log.d("HistoryRepositoryImpl", "addHistory() called with trackId=$trackId, storageKey=$storageKey")

            val request = HistoryRequest(trackId = trackId, storageKey = storageKey)
            Log.d("HistoryRepositoryImpl", "Request body: $request")

            val response = historyApi.addHistory(request)
            Log.d("HistoryRepositoryImpl", "addHistory() response: $response")

            response
        } catch (e: Exception) {
            Log.e("HistoryRepositoryImpl", "addHistory() error: ${e.message}", e)
            HistoryResponse("", "", "", "", Date())
        }
    }

}
