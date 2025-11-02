package com.example.frontend.domain.repositories

import com.example.frontend.data.models.history.HistoryResponse
import com.example.frontend.data.models.search.SearchHistoryResponse

interface HistoryRepository {
    suspend fun getSearchHistory(): List<SearchHistoryResponse>

    suspend fun getHistory(): List<HistoryResponse>

    suspend fun addHistory(trackId: String, storageKey: String): HistoryResponse

}