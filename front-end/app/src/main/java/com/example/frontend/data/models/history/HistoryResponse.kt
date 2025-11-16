package com.example.frontend.data.models.history

import java.util.Date

data class HistoryResponse(
    val id: String,
    val trackId: String,
    val email: String,
    val storageKey: String,
    val playedAt: Date
)