package com.example.frontend.ui.home

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.HistoryRepositoryImpl
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.HistoryRepository
import com.example.frontend.domain.repositories.PlayingRepository
import com.example.frontend.domain.repositories.TrackRepository
import com.example.frontend.ui.profile.ProfileViewModel
import com.example.frontend.ui.profile.ProfileViewModelFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
data class TrackUiModel(
    val trackInfo: GetTracksResponse,
    val coverImageUrl: String?
)
class HomeViewModel(context: Context) : ViewModel() {
    private val repo: TrackRepository = TrackRepositoryImpl(ApiClient.trackApi)
    private val playingRepo: PlayingRepository = PlayingRepositoryImpl(ApiClient.playingApi)

    private val prefs = AppPreferences(context)

    private val historyRepo: HistoryRepository = HistoryRepositoryImpl(ApiClient.historyApi, prefs)
    private val _tracks = MutableStateFlow<List<GetTracksResponse>>(emptyList())

    private val _uiTracks = MutableStateFlow<List<TrackUiModel>>(emptyList())
    val uiTracks = _uiTracks.asStateFlow()
    val tracks = _tracks.asStateFlow()

    private val _history = MutableStateFlow<List<TrackUiModel>>(emptyList())
    val history = _history.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadTracks(limit: Int = 10) {
        if (_tracks.value.isNotEmpty()) {
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val initialTracks = repo.getTracks().take(limit)

                val tracksWithUrls = initialTracks.map { track ->
                    async {
                        val response = playingRepo.getUrlTrack(track.storageKey, track.coverImageKey)
                        TrackUiModel(trackInfo = track, coverImageUrl = response.coverImageUrl)
                    }
                }.awaitAll()

                _uiTracks.value = tracksWithUrls
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadHistory(limit: Int = 10) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "loadHistory() called")
            _isLoading.value = true
            try {
                val history = historyRepo.getHistory()
                    .filter { !it.trackId.isNullOrBlank() }
                    .groupBy { it.trackId }
                    .mapValues { (_, list) -> list.maxByOrNull { it.playedAt }!! }
                    .values
                    .sortedByDescending { it.playedAt }
                    .take(8)


                val historyTracks = history.map { historyItem ->
                    async {
                        val historyItemSongs = repo.getTrackById(historyItem.trackId)
                        val response = playingRepo.getUrlTrack(historyItemSongs.storageKey, historyItemSongs.coverImageKey)
                        TrackUiModel(trackInfo = historyItemSongs, coverImageUrl = response.coverImageUrl)
                    }
                }.awaitAll()
                _history.value = historyTracks
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}