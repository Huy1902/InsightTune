package com.example.frontend.data.models.home

data class GetTracksResponse (
    val id: String,
    val title: String,
    val artists: List<String>? = null,
    val albumId: String,
    val storageKey: String,
    val durationMs: Long,
    val coverImageKey: String?
)