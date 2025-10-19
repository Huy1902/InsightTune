package com.example.frontend.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.domain.repositories.FavoriteRepository
import com.example.frontend.domain.repositories.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val favoriteRepository: FavoriteRepository,
    private val trackRepository: TrackRepository
) : ViewModel() {
    private val _favoriteTracks = MutableStateFlow<List<GetTracksResponse>>(emptyList())
    val favoriteTracks: StateFlow<List<GetTracksResponse>> = _favoriteTracks

    fun loadFavorites() {
        viewModelScope.launch {
            try {
                val favoriteIds = favoriteRepository.getFavorites()

            }
        }
    }
}