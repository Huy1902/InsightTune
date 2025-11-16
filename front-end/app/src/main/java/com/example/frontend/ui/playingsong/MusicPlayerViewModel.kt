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
import com.example.frontend.data.models.playingsong.NextTracksResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.FavoriteRepository
import com.example.frontend.domain.repositories.HistoryRepository
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
//    trackList: List<NextTracksResponse> = emptyList(),
//    currentIndex: Int,
    private val favoriteRepo: FavoriteRepository,
    private val playingRepo: PlayingRepository,
    private val historyRepo: HistoryRepository,
    context: Context
) : ViewModel() {

    private var isFavoriteShuffleEnabled = false
    private var originalFavoriteTracks: List<NextTracksResponse> = emptyList()
    private var title: String = ""
    private var artist: String = ""
    private var trackId: String = ""
    private var position: Int = 0
    private val tracksRepo : TrackRepository = TrackRepositoryImpl(ApiClient.trackApi)

    private var _playerState = MutableStateFlow(PlayerState())

    private val nextTracks: MutableList<NextTracksResponse> = mutableListOf()

    private val _history = MutableStateFlow<List<NextTracksResponse>>(emptyList())

    private val _isRepeatOne = MutableStateFlow(false)
    val isRepeatOne = _isRepeatOne.asStateFlow()

    private var currentIndexSong: Int = -1

    private var controller: MediaController? = null

    private var currentSongUrl: String = ""

    private val appContext = context.applicationContext

    val playerState = _playerState.asStateFlow()

    private var canFetchNextTracks: Boolean = true

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
            canFetchNextTracks = true

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
            historyRepo.addHistory(newTrackId, newUrlKey)
        }
    }
    fun setPlaylist(newTracks: List<NextTracksResponse>, startIndex: Int, allowFetching: Boolean) {
        viewModelScope.launch {
            nextTracks.clear()
            nextTracks.addAll(newTracks)
            currentIndexSong = startIndex
            canFetchNextTracks = allowFetching // <-- Cờ quan trọng nhất

            if (currentIndexSong in nextTracks.indices) {
                val track = nextTracks[currentIndexSong]
                loadPlaying(track)
            }
        }
    }

    fun setCurrentIndex(index: Int) {
        currentIndexSong = index
    }

    init {
        viewModelScope.launch {
            setupMediaController()
            delay(500)
            restoreLastPlayback()
            startProgressUpdater()

            FavoriteEventBus.favoriteChanged.collect { changedTrackId ->
                if (changedTrackId == getCurrentTrack()?.id) {
                    checkIfFavorite(changedTrackId)
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
            putExtra("track_id", trackId)
            putExtra("resume_position", position)
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

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        viewModelScope.launch {
                            if (_isRepeatOne.value) {
                                Log.d("MusicPlayerVM", "Repeat one → replay current track")
                                getCurrentTrack()?.let { loadPlaying(it) }
                            } else {
                                playNextTrack()
                            }
                        }
                    }
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
                trackId = track.id
                Log.d("MusicDebug", "Track URL: ${response.trackUrl}")
                _playerState.value = _playerState.value.copy(
                    mediaMetadata = MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artists.joinToString(", "))
                        .build(),
                    coverImageUrl = response.coverImageUrl
                )
                //loadCoverImage(track.coverImageKey)
                checkIfFavorite(track.id)
                startMusicService(appContext, currentSongUrl!!)
                //historyRepo.addHistory(track.id, track.storageKey)
                Log.d("MusicPlayerVM", "Add to history: ${track.id}")
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
            if (nextTracks.isEmpty()) return@launch

            if (currentIndexSong + 1 >= nextTracks.size) {
                if (canFetchNextTracks) {
                    fetchNextTracks()

                    if (currentIndexSong + 1 >= nextTracks.size) {
                        currentIndexSong = -1
                    }
                } else {
                    currentIndexSong = -1
                }
            }
            currentIndexSong++
            val nextTrack = nextTracks[currentIndexSong]
            loadPlaying(nextTrack)
        }
    }

    fun playPreviousTrack() {
        viewModelScope.launch {
            if (nextTracks.isEmpty()) return@launch

            if (currentIndexSong - 1 >= 0) {
                currentIndexSong--
            } else {
                currentIndexSong = nextTracks.size - 1
            }
            val prevTrack = nextTracks[currentIndexSong]
            loadPlaying(prevTrack)
        }
    }

    fun toggleFavoriteShuffle() {
        viewModelScope.launch {
            isFavoriteShuffleEnabled = !isFavoriteShuffleEnabled
            if (isFavoriteShuffleEnabled) {
                originalFavoriteTracks = nextTracks.toList()
                nextTracks.shuffle()
            } else {
                if (originalFavoriteTracks.isNotEmpty()) {
                    nextTracks.clear()
                    nextTracks.addAll(originalFavoriteTracks)
                }
            }

            if (currentIndexSong >= nextTracks.size) {
                currentIndexSong = 0
            }

            val current = getCurrentTrack()
            if (current != null) {
                _playerState.value = _playerState.value.copy(
                    mediaMetadata = _playerState.value.mediaMetadata
                        .buildUpon()
                        .setTitle(current.title)
                        .setArtist(current.artists.joinToString(", "))
                        .build()
                )
            }
        }
    }

    fun isFavoriteShuffleOn(): Boolean = isFavoriteShuffleEnabled

    fun toggleRepeatOne() {
        _isRepeatOne.value = !_isRepeatOne.value
        Log.d("MusicPlayerVM", "Repeat One mode: ${_isRepeatOne.value}")
    }

    fun updateUserState() {
        viewModelScope.launch {
            val current = getCurrentTrack() ?: return@launch
            playingRepo.updateUserState(current.id, _playerState.value.currentPosition.toInt())
        }
    }

    fun restoreLastPlayback() {
        viewModelScope.launch {
            try {
                val state = playingRepo.getUserState()
                val trackId = state.trackId
                val position = state.positionMs.toLong()

                if (trackId.isNotEmpty()) {
                    Log.d("MusicPlayerVM", "Restoring trackId=$trackId at $position ms")
                    val track = tracksRepo.getTrackById(trackId)
                    if (track != null) {
                        val resumedTrack = NextTracksResponse(
                            id = track.id,
                            title = track.title,
                            artists = track.artists ?: listOf("Unknown artist"),
                            albumId = track.albumId,
                            storageKey = track.storageKey,
                            durationMs = track.durationMs,
                            coverImageKey = track.coverImageKey ?: ""
                        )

                        nextTracks.clear()
                        nextTracks.add(resumedTrack)
                        currentIndexSong = 0

                        loadPlaying(resumedTrack)

                        var waited = 0
                        while ((controller?.playbackState ?: Player.STATE_IDLE) != Player.STATE_READY && waited < 10) {
                            delay(300)
                            waited++
                        }

                        controller?.seekTo(position)
                        controller?.pause()
                        Log.d("MusicPlayerVM", "Restored to $position ms (paused)")

                        if (canFetchNextTracks) {
                            try {
                                val nextList = tracksRepo.nextTracks(currentTrackId = resumedTrack.id)
                                if (nextList.isNotEmpty()) {
                                    nextTracks.addAll(nextList)
                                    Log.d("MusicPlayerVM", "Fetched ${nextList.size} next tracks after restore")
                                }
                            } catch (e: Exception) {
                                Log.e("MusicPlayerVM", "Failed to fetch next tracks: ${e.message}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicPlayerVM", "Failed to restore playback: ${e.message}")
            }
        }
    }
}
