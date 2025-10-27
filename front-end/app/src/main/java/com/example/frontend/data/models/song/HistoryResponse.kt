package com.example.frontend.data.models.song

import java.util.Date

data class HistoryResponse(
    val id: String,
    val trackId: String,
    val email: String,
    val storageKey: String,
    val playedAt: Date
)