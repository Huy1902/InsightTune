package com.example.frontend.data.models.song

data class GetTracksResponse (
    val title: String,
    val storageKey: String,
    val durationMs: Long,
    val coverImageKey: String?
)