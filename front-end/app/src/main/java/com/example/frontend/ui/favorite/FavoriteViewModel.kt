package com.example.frontend.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.domain.repositories.FavoriteRepository
import com.example.frontend.domain.repositories.PlayingRepository
import com.example.frontend.domain.repositories.TrackRepository
import com.example.frontend.ui.home.TrackUiModel
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
    fun loadFavorites() {
        viewModelScope.launch {
            val initialTracks = favoriteRepository.getFavorites()

            val tracksWithUrls = initialTracks.map { track ->
                async {
                    val response = playingRepo.getUrlTrack(track.storageKey, track.coverImageKey)
                    TrackUiModel(trackInfo = track, coverImageUrl = response.coverImageUrl)
                }
            }.awaitAll()

            _uiTracks.value = tracksWithUrls
        }
    }
}