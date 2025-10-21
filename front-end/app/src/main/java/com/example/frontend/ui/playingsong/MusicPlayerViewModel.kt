package com.example.frontend.ui.playingsong

import android.R
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.models.song.PlayingResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.FavoriteRepositoryImpl
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.FavoriteRepository
import com.example.frontend.domain.repositories.PlayingRepository
import com.example.frontend.domain.repositories.TrackRepository
import com.example.frontend.service.MusicService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
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

class MusicPlayerViewModel @OptIn(androidx.media3.common.util.UnstableApi::class) constructor
    (
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

    private var controller: MediaController? = null


    private var currentSongUrl: String = ""

    private val appContext = context.applicationContext

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
        viewModelScope.launch {
            setupMediaController()
            loadPlaying()
            loadCoverImage()
            checkIfFavorite()
            startProgressUpdater()
        }
    }


    @OptIn(UnstableApi::class)
    private fun startMusicService(context: Context, songUrl: String?) {
        if (songUrl.isNullOrEmpty()) return

        val intent = Intent(context, MusicService::class.java).apply {
            putExtra("song_url", songUrl)
            putExtra("song_title", title)
            putExtra("song_artist", artist)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun setupMediaController() {
        try {
            val sessionToken = SessionToken(appContext, ComponentName(appContext, MusicService::class.java))
            controller = MediaController.Builder(appContext, sessionToken).buildAsync().await()

            controller?.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                }

                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    _playerState.value = _playerState.value.copy(mediaMetadata = mediaMetadata)
                }
            })
        } catch (e: Exception) {
            Log.e("MusicPlayerVM", "Failed to create MediaController: ${e.message}")
        }
    }

    private fun startProgressUpdater() {
        viewModelScope.launch {
            while (isActive) {
                controller?.let {
                    _playerState.value = _playerState.value.copy(
                        currentPosition = it.currentPosition,
                        totalDuration = if (it.duration > 0) it.duration else 0L
                    )
                }
                delay(500)
            }
        }
    }

    private fun checkIfFavorite() {
        if (trackId.isBlank()) return

        viewModelScope.launch {
            try {
                val isFav = favoriteRepo.isFavorite(trackId)
                _playerState.value = _playerState.value.copy(isFavorite = isFav)
            } catch (e: Exception) {
                Log.e("MusicPlayerVM", "Không thể kiểm tra trạng thái favorite: ${e.message}", e)
                _playerState.value = _playerState.value.copy(isFavorite = false)
            }
        }
    }

    private fun loadPlaying() {
        if (urlKey.isBlank()) return

        viewModelScope.launch {
            try {
                val response = playingRepo.getUrlTrack(urlKey, imageKey)
                if (response.trackUrl.isNotEmpty()) {
                    currentSongUrl = response.trackUrl
                    Log.d("MusicDebug", "Track URL: ${response.trackUrl}")

                    startMusicService(appContext, currentSongUrl!!)
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerVM", "Error loading track URL: ${e.message}")
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
        controller?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun seekToPosition(position: Long) {
        viewModelScope.launch {
            val ctrl = controller
            if (ctrl != null && ctrl.duration > 0) {
                try {
                    ctrl.seekTo(position)
                    Log.d("PlayerSeek", "Seek to position: $position")
                } catch (e: Exception) {
                    Log.e("PlayerSeek", "Seek failed: ${e.message}")
                }
            } else {
                Log.w("PlayerSeek", "Controller not ready or no media loaded")
            }
        }
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
}
