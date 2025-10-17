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
import com.example.frontend.data.models.song.PlayingResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.PlayingRepository
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
    val currentTrack: GetTracksResponse? = null,
    val isFavorite: Boolean = false
)

class MusicPlayerViewModel (
    private val urlKey: String,
    private val title: String,
    private val artist: String,
    private val imageKey: String,
    private val context: Context
) : ViewModel() {

    private val playingRepo: PlayingRepository = PlayingRepositoryImpl(ApiClient.playingApi)
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
        loadPlaying()
    }
   // val urlR = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

    fun loadPlaying() {
        // Kiểm tra key hợp lệ
        if (urlKey.isBlank()) return

        viewModelScope.launch {
            // Gọi API để lấy URL thật
            val response = playingRepo.getUrlTrack(urlKey, imageKey)

            // Chỉ chạy nhạc khi có URL hợp lệ
            if (response.trackUrl.isNotEmpty()) {
                val mediaItem = MediaItem.fromUri(response.trackUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                // exoPlayer.play() // Có thể bạn muốn người dùng tự bấm play
            }
        }
    }

    fun getTitle(): String {
        return title
    }

    fun getImage(): String {
        return ""
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

    fun onToggleFavorite() {
        // Logic để thay đổi trạng thái isFavorite trong _playerState
        val currentState = _playerState.value.isFavorite
        _playerState.value = _playerState.value.copy(isFavorite = !currentState)
        // TODO: Gọi repository để lưu trạng thái này vào database/server
    }

    override fun onCleared() {
        exoPlayer.removeListener(listener)
        exoPlayer.release()
    }
}
