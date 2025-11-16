package com.example.frontend.ui.favorite

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object FavoriteEventBus {
    private val _favoriteChanged = MutableSharedFlow<String>(replay = 0)
    val favoriteChanged = _favoriteChanged.asSharedFlow()

    suspend fun emitFavoriteChange(trackId: String) {
        _favoriteChanged.emit(trackId)
    }
}
