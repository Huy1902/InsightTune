package com.example.frontend.ui.playingsong

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY
)

class MusicPlayerViewModel(private val exoPlayer: ExoPlayer) : ViewModel() {

    private val _playerState = MutableStateFlow(PlayerState())

    val playerState = _playerState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            _playerState.value = _playerState.value.copy(mediaMetadata = mediaMetadata)
        }
    }

    init {
        exoPlayer.addListener(listener)

        viewModelScope.launch {
            while (isActive) {
                _playerState.value = _playerState.value.copy(
                    currentPosition = exoPlayer.currentPosition,
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                )
                delay(1000)
            }
        }
    }

    fun loadAndPlaySong(url: String) {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)

        exoPlayer.prepare()

        exoPlayer.play()
        Log.d("PlayerDebug", "Bài hát đã được tải và phát")
    }

    fun onPlayPauseClick() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        Log.d("PlayerDebug", "Nút Play/Pause đã được ấn")
    }

    fun seekToPosition(position : Long) {
        exoPlayer.seekTo(position)
    }


    override fun onCleared() {
        exoPlayer.removeListener(listener)
        exoPlayer.release()
    }
}
