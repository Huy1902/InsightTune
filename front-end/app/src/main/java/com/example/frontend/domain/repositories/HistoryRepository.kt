package com.example.frontend.domain.repositories

import com.example.frontend.data.models.song.SearchHistoryResponse

interface HistoryRepository {
    suspend fun getSearchHistory(): List<SearchHistoryResponse>
}