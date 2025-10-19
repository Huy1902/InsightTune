package com.example.frontend.data.remote

import android.util.Log
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.song.SearchHistoryResponse
import com.example.frontend.domain.repositories.HistoryRepository
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
}
