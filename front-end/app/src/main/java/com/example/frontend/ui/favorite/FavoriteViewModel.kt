package com.example.frontend.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.models.home.GetTracksResponse
import com.example.frontend.domain.repositories.FavoriteRepository
import com.example.frontend.domain.repositories.PlayingRepository
import com.example.frontend.domain.repositories.TrackRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrackUiModel(
    val trackInfo: GetTracksResponse,
    val coverImageUrl: String?
)

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val playingRepo: PlayingRepository
) : ViewModel() {
    private val _uiTracks = MutableStateFlow<List<TrackUiModel>>(emptyList())
    val uiTracks = _uiTracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            val initialTracks = favoriteRepository.getFavorites()

            val tracksWithUrls = initialTracks.map { track ->
                async {
                   // val response = playingRepo.getUrlTrack(track.storageKey, track.coverImageKey)
                    val response = playingRepo.getImage(track.coverImageKey ?: "")
                    TrackUiModel(trackInfo = track, coverImageUrl = response.url)
                }
            }.awaitAll()
            _uiTracks.value = tracksWithUrls
            _isLoading.value = false
        }
    }

    fun deleteFavorite(trackId: String) {
        viewModelScope.launch {
            favoriteRepository.deleteFavorite(trackId)
            FavoriteEventBus.emitFavoriteChange(trackId)
            loadFavorites()
        }
    }
}