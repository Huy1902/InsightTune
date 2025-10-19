package com.example.frontend.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.models.song.SearchHistoryResponse
import com.example.frontend.domain.repositories.HistoryRepository
import com.example.frontend.domain.repositories.TrackRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class SearchViewModel(
    private val trackRepository: TrackRepository,
    private val historyRepository: HistoryRepository,
    private val prefs: AppPreferences
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<GetTracksResponse>>(emptyList())
    val searchResults: StateFlow<List<GetTracksResponse>> = _searchResults

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
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}