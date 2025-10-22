package com.example.frontend.data.models.song

data class NextTracksResponse (
    val id: String,
    val title: String,
    val artists: List<String>,
    val albumId: String,
    val storageKey: String,
    val durationMs: Long,
    val coverImageKey: String
)