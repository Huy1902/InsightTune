package com.example.frontend.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlin.coroutines.coroutineContext

class PlayingViewModelFactory (private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlayingViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
