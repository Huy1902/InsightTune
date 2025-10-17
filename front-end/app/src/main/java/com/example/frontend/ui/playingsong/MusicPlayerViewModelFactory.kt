package com.example.frontend.ui.playingsong

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.exoplayer.ExoPlayer

class MusicPlayerViewModelFactory (private val url: String, private val title: String, private val artist: String, private val imageUrl: String, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MusicPlayerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MusicPlayerViewModel(url, title, artist, imageUrl, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}