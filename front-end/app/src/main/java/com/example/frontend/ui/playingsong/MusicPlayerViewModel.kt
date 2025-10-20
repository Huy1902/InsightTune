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
import com.example.frontend.data.remote.FavoriteRepositoryImpl
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.FavoriteRepository
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
    val coverImageUrl: String? = null,
    val currentTrack: GetTracksResponse? = null,
    val isFavorite: Boolean = false
)

class MusicPlayerViewModel (
    private val trackId: String,
    private val favoriteRepo: FavoriteRepository,
    private val playingRepo: PlayingRepository,
    private val urlKey: String,
    private val title: String,
    private val artist: String,
    private val imageKey: String,
    context: Context
) : ViewModel() {

    private val tracksRepo : TrackRepository = TrackRepositoryImpl(ApiClient.trackApi)
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
        loadPlaying()
        loadCoverImage()
     //   checkIfFavorite()
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

    private fun checkIfFavorite() {
        viewModelScope.launch {
            try {
                val favorites = favoriteRepo.getFavorites()
                val isFav = favorites.any { it.id == trackId }
                _playerState.value = _playerState.value.copy(isFavorite = isFav)
            } catch (e: Exception) {
                Log.e("MusicPlayerVM", "Failed to check favorite status", e)
            }
        }
    }
    fun loadPlaying() {
        if (urlKey.isBlank()) return

        viewModelScope.launch {
            val response = playingRepo.getUrlTrack(urlKey, imageKey)

            if (response.trackUrl.isNotEmpty()) {
                val mediaItem = MediaItem.fromUri(response.trackUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
            }
        }
    }

    private fun loadCoverImage() {
        if (imageKey.isBlank() || imageKey == "no_image") return

        viewModelScope.launch {
            try {
                val response = playingRepo.getImage(imageKey)
                if (response.url.isNotEmpty()) {
                    _playerState.value = _playerState.value.copy(coverImageUrl = response.url)
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerVM", "Error: ${e.message}")
            }
        }
    }


    fun getTitle(): String {
        return title
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
        viewModelScope.launch {
            val isCurrentlyFavorite = _playerState.value.isFavorite
            try {
                if (isCurrentlyFavorite) {
                    favoriteRepo.deleteFavorite(trackId)
                    Log.d("FavoriteDebug", "Favorite deleted via API")
                } else {
                    favoriteRepo.addFavorite(trackId)
                    Log.d("FavoriteDebug", "Favorite added via API")
                }
                _playerState.value = _playerState.value.copy(isFavorite = !isCurrentlyFavorite)
            } catch (e: Exception) {
                Log.e("MusicPlayerVM", "Failed to toggle favorite", e)
            }
        }
    }


    override fun onCleared() {
        exoPlayer.removeListener(listener)
        exoPlayer.release()
    }
}
