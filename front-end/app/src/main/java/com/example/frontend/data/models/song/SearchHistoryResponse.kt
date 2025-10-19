package com.example.frontend.data.models.song

import java.util.Date

data class SearchHistoryResponse (
    val id: Int,
    val email: String,
    val search: String,
    val searchAt: Date
)