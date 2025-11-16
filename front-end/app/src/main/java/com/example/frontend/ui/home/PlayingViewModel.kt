package com.example.frontend.ui.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frontend.data.models.playingsong.PlayingResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.domain.repositories.PlayingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayingViewModel (
  context: Context
): ViewModel() {
    private val playingRepo: PlayingRepository = PlayingRepositoryImpl(ApiClient.playingApi)

    private val _playing = MutableStateFlow<PlayingResponse?>(null)

    val playing = _playing.asStateFlow()

    fun loadUrl(urlKey: String, urlImage: String?) {
        viewModelScope.launch {
            _playing.value = playingRepo.getUrlTrack(urlKey, urlImage)
            Log.d("PLAYING", "Loaded playing: ${playing.value?.trackUrl}")
        }
    }

}