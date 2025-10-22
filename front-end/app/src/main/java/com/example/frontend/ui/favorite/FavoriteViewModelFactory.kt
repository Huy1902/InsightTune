package com.example.frontend.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.frontend.domain.repositories.FavoriteRepository
import com.example.frontend.domain.repositories.PlayingRepository

class FavoriteViewModelFactory(
    private val favoriteRepository: FavoriteRepository,
    private val playingRepo: PlayingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            return FavoriteViewModel(favoriteRepository, playingRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}