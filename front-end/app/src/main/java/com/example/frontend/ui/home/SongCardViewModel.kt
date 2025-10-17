package com.example.frontend.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.models.song.PlayingResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.domain.repositories.PlayingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// State để chứa dữ liệu cho SongCard
data class SongCardUiState(
    val coverImageUrl: String? = null,
    val isLoading: Boolean = true
)

class SongCardViewModel(
    storageKey: String,
    coverImageKey: String?
) : ViewModel() {

    private val playingRepo: PlayingRepository = PlayingRepositoryImpl(ApiClient.playingApi)
    private val _uiState = MutableStateFlow(SongCardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Gọi API ngay khi ViewModel được tạo
        viewModelScope.launch {
            val response = playingRepo.getUrlTrack(storageKey, coverImageKey)
            _uiState.value = SongCardUiState(
                coverImageUrl = response.coverImageUrl,
                isLoading = false
            )
        }
    }
}

// Factory để tạo SongCardViewModel với tham số
class SongCardViewModelFactory(
    private val storageKey: String,
    private val coverImageKey: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SongCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SongCardViewModel(storageKey, coverImageKey) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}