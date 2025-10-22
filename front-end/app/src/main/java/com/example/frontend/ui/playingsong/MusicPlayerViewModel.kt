package com.example.frontend.ui.playingsong

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.frontend.data.models.song.NextTracksResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.FavoriteRepository
import com.example.frontend.domain.repositories.PlayingRepository
import com.example.frontend.domain.repositories.TrackRepository
import com.example.frontend.service.MusicService
import com.example.frontend.ui.favorite.FavoriteEventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L,
    val mediaMetadata: MediaMetadata = MediaMetadata.EMPTY,
    val coverImageUrl: String? = null,
//    val currentTrack: GetTracksResponse? = null,
    val isFavorite: Boolean = false
)

class MusicPlayerViewModel @OptIn(androidx.media3.common.util.UnstableApi::class) constructor
    (
    trackList: List<NextTracksResponse> = emptyList(),
    currentIndex: Int,
    private val favoriteRepo: FavoriteRepository,
    private val playingRepo: PlayingRepository,
    context: Context
) : ViewModel() {

    private var trackId: String = ""
    private var urlKey: String = ""
    private var title: String = ""
    private var artist: String = ""
    private var imageKey: String = ""

    private val tracksRepo : TrackRepository = TrackRepositoryImpl(ApiClient.trackApi)
    private var _playerState = MutableStateFlow(PlayerState())

    private val nextTracks: MutableList<NextTracksResponse> = mutableListOf()

    private var currentIndexSong: Int = -1

    private var controller: MediaController? = null

    private var currentSongUrl: String = ""

    private val appContext = context.applicationContext

    val playerState = _playerState.asStateFlow()

    private fun getCurrentTrack(): NextTracksResponse? {
        return if (currentIndexSong in nextTracks.indices) {
            nextTracks[currentIndexSong]
        } else {
            null
        }
    }
    private val currentTrack = getCurrentTrack()

    fun playSong(
        newTrackId: String,
        newUrlKey: String,
        newTitle: String,
        newArtist: String,
        newImageKey: String
    ) {
        viewModelScope.launch {
            val index = nextTracks.indexOfFirst { it.id == newTrackId }
            if (index != -1) {
                currentIndexSong = index
                val track = nextTracks[currentIndexSong]
                loadPlaying(track)
                checkIfFavorite(track.id)
            } else {
                val single = NextTracksResponse(
                    id = newTrackId,
                    title = newTitle,
                    artists = listOf(newArtist),
                    albumId = "",
                    storageKey = newUrlKey,
                    durationMs = 0,
                    coverImageKey = newImageKey
                )
                currentIndexSong = 0
                nextTracks.clear()
                nextTracks.add(single)
                loadPlaying(single)
            }
        }
    }

    fun setCurrentIndex(index: Int) {
        currentIndexSong = index
    }

    init {
        viewModelScope.launch {
            nextTracks.addAll(trackList)
            setupMediaController()
            if (currentTrack != null) loadPlaying(currentTrack)
            startProgressUpdater()
            FavoriteEventBus.favoriteChanged.collect { changedTrackId ->
                if (changedTrackId == trackId) {
                    checkIfFavorite(trackId)
                }
            }
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

    private fun checkIfFavorite(trackId: String) {
        if (trackId.isBlank()) return
        viewModelScope.launch {
            try {
                val favorites = favoriteRepo.getFavorites()
                val isFav = favorites.any { it.id == trackId }
                _playerState.value = _playerState.value.copy(isFavorite = isFav)
            } catch (e: Exception) {
                Log.e("MusicPlayerVM", "Cannot check favorite: ${e.message}")
            }
        }
    }
    private suspend fun loadPlaying(track: NextTracksResponse) {
        try {
            val response = playingRepo.getUrlTrack(track.storageKey, track.coverImageKey)
            if (response.trackUrl.isNotEmpty()) {
                currentSongUrl = response.trackUrl
                title = track.title
                artist = track.artists.joinToString(", ")
                Log.d("MusicDebug", "Track URL: ${response.trackUrl}")
                _playerState.value = _playerState.value.copy(
                    mediaMetadata = MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artists.joinToString(", "))
                        .build()
                )
                loadCoverImage(track.coverImageKey)
                checkIfFavorite(track.id)
                startMusicService(appContext, currentSongUrl!!)
            }
        } catch (e: Exception) {
            Log.e("MusicPlayerVM", "Error loading track URL: ${e.message}")
        }
    }



    private fun loadCoverImage(imageKey: String) {
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

    private suspend fun fetchNextTracks() {
        val lastTrackId = nextTracks.lastOrNull()?.id ?: return
        try {
            val response = tracksRepo.nextTracks(currentTrackId = lastTrackId)
            if (response.isNotEmpty()) {
                nextTracks.addAll(response)
                Log.d("MusicPlayerVM", "Fetched ${response.size} next tracks.")
            }
        } catch (e: Exception) {
            Log.e("MusicPlayerVM", "Error fetching next tracks: ${e.message}")
        }
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
            val current = getCurrentTrack() ?: return@launch
            val isFav = _playerState.value.isFavorite
            try {
                if (isFav) favoriteRepo.deleteFavorite(current.id)
                else favoriteRepo.addFavorite(current.id)
                _playerState.value = _playerState.value.copy(isFavorite = !isFav)
            } catch (e: Exception) {
                Log.e("MusicPlayerVM", "Failed to toggle favorite", e)
            }
        }
    }


    fun playNextTrack() {
        viewModelScope.launch {
            if (nextTracks.isEmpty()) {
                fetchNextTracks()
                if (nextTracks.isEmpty()) return@launch
            }
            currentIndexSong++
            if (currentIndexSong >= nextTracks.size) {
                fetchNextTracks()
            }
            val nextTrack = nextTracks[currentIndexSong]
            loadPlaying(
                NextTracksResponse(
                    id = nextTrack.id,
                    title = nextTrack.title,
                    artists = nextTrack.artists,
                    albumId = nextTrack.albumId,
                    storageKey = nextTrack.storageKey,
                    durationMs = nextTrack.durationMs,
                    coverImageKey = nextTrack.coverImageKey
                )
            )
        }

    }

    fun playPreviousTrack() {
        viewModelScope.launch {
            currentIndexSong--
            if (currentIndexSong < 0) currentIndexSong = 0
            val prevTrack = nextTracks[currentIndexSong]
            loadPlaying(
                NextTracksResponse (
                    id = prevTrack.id,
                    title = prevTrack.title,
                    artists = prevTrack.artists,
                    albumId = prevTrack.albumId,
                    storageKey = prevTrack.storageKey,
                    durationMs = prevTrack.durationMs,
                    coverImageKey = prevTrack.coverImageKey
                )
            )
        }
    }
}
