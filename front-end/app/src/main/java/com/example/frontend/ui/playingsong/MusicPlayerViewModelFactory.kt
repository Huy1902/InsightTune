package com.example.frontend.ui.playingsong

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.frontend.data.models.song.NextTracksResponse
import com.example.frontend.domain.repositories.FavoriteRepository
import com.example.frontend.domain.repositories.PlayingRepository

class MusicPlayerViewModelFactory(
    private val trackList: List<NextTracksResponse> = emptyList(),
    private val currentIndex: Int = 0,
    private val favoriteRepo: FavoriteRepository,
    private val playingRepo: PlayingRepository,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicPlayerViewModel(
                trackList = trackList,
                currentIndex = currentIndex,
                favoriteRepo = favoriteRepo,
                playingRepo = playingRepo,
                context = context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class for MusicPlayerViewModelFactory")
    }
}