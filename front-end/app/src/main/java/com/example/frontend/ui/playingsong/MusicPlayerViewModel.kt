package com.example.frontend.ui.playingsong

import android.R
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.TrackRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import retrofit2.http.Url

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
    val currentTrack: GetTracksResponse? = null
)

class MusicPlayerViewModel (
    private val url: String,
    private val title: String,
    private val artist: String,
    private val imageUrl: String,
    private val context: Context
) : ViewModel() {

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
    val exoPlayer = ExoPlayer.Builder(context).build()

    init {
        exoPlayer.addListener(listener)
        viewModelScope.launch {

            while (isActive) {

                _playerState.value = _playerState.value.copy(
                    currentPosition = exoPlayer.currentPosition,
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                )
                delay(500)
            }
        }
    }
//    val urlR = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

    fun loadAndPlaySong() {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)

        exoPlayer.prepare()

        exoPlayer.play()
        Log.d("PlayerDebug", "Bài hát đã được tải và phát")
    }

    fun getTitle(): String {
        return title
    }

    fun getImage(): String {
        return imageUrl
    }

    fun getArtist(): String {
        return artist
    }
    fun onPlayPauseClick() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
        Log.d("PlayerDebug", "Nút Play/Pause đã được ấn")
    }

    fun onPlayNextSong() {
        exoPlayer.seekToNext()
        exoPlayer.play()
    }

    fun seekToPosition(position : Long) {
        exoPlayer.seekTo(position)
    }


    override fun onCleared() {
        exoPlayer.removeListener(listener)
        exoPlayer.release()
    }
}
