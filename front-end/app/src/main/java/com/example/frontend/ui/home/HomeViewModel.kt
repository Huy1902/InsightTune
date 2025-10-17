package com.example.frontend.ui.home

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.models.song.PlayingResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.PlayingRepository
import com.example.frontend.domain.repositories.TrackRepository
import com.example.frontend.ui.profile.ProfileViewModel
import com.example.frontend.ui.profile.ProfileViewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(context: Context) : ViewModel() {
    private val repo: TrackRepository = TrackRepositoryImpl(ApiClient.trackApi)

    private val _tracks = MutableStateFlow<List<GetTracksResponse>>(emptyList())
    val tracks = _tracks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun loadTracks(limit: Int = 10) {
        viewModelScope.launch {
            _isLoading.value = true
            val allTracks = repo.getTracks()
            _tracks.value = allTracks.take(limit)
            _isLoading.value = false
        }
    }

}