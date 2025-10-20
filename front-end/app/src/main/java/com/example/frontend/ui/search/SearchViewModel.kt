package com.example.frontend.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.models.song.SearchHistoryResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.domain.repositories.HistoryRepository
import com.example.frontend.domain.repositories.PlayingRepository
import com.example.frontend.domain.repositories.TrackRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

data class TrackUiModel(
    val trackInfo: GetTracksResponse,
    val coverImageUrl: String?
)

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val trackRepository: TrackRepository,
    private val historyRepository: HistoryRepository,
    private val prefs: AppPreferences
) : ViewModel() {
    private val playingRepo: PlayingRepository = PlayingRepositoryImpl(ApiClient.playingApi)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<TrackUiModel>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _recentSearches = MutableStateFlow<List<SearchHistoryResponse>>(emptyList())
    val recentSearches = _recentSearches.asStateFlow()

    init {
        loadRecentSearches()

        viewModelScope.launch {
            _searchQuery
                .debounce(500L)
                .collect { query ->
                    if (query.isNotBlank()) {
                        searchTracks(query)
                    } else {
                        _searchResults.value = emptyList()
                        _isLoading.value = false
                    }
                }
        }
    }

    fun loadRecentSearches(limit: Int = 10) {
        viewModelScope.launch {
            try {
                val history = historyRepository.getSearchHistory()
                _recentSearches.value = history.take(limit)
            } catch (e: Exception) {
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    private fun searchTracks(keyword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val results = trackRepository.searchTracks(keyword)
                val tracksWithUrls = results.map { track ->
                    async {
                        val response = playingRepo.getUrlTrack(track.storageKey, track.coverImageKey)
                        TrackUiModel(trackInfo = track, coverImageUrl = response.coverImageUrl)
                    }
                }.awaitAll()
                _searchResults.value = tracksWithUrls
            } catch (e: Exception) {
            } finally {
                _isLoading.value = false
            }
        }
    }
}