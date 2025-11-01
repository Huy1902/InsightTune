package com.example.frontend.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.app.NotificationCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.frontend.R
import androidx.media3.common.C
import androidx.media3.common.Player
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.domain.repositories.PlayingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@UnstableApi
class MusicService : MediaSessionService() {

    private val playingRepo: PlayingRepository = PlayingRepositoryImpl(ApiClient.playingApi)
    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var playerNotificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        if (playerInstance == null) {
            playerInstance = ExoPlayer.Builder(this)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    /* handleAudioFocus= */ true
                )
                .setHandleAudioBecomingNoisy(true)
                .build()
        }

        player = playerInstance

        player?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying) {
                    persistPlaybackState()
                }
            }
        })

        mediaSession = MediaSession.Builder(this, player!!)
            .setId("SpotubeSession")
            .build()

        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(DescriptionAdapter(this))
            .setSmallIconResourceId(com.example.frontend.R.drawable.music_note)
            .setChannelImportance(NotificationManager.IMPORTANCE_LOW)
            .build()
            .apply {
                setMediaSessionToken(mediaSession!!.sessionCompatToken)
                setPlayer(player)
            }

        startForeground(NOTIFICATION_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            player?.stop()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildNotification())

        val songUrl = intent?.getStringExtra("song_url")
        val songTitle = intent?.getStringExtra("song_title") ?: "Unknown"
        val songArtist = intent?.getStringExtra("song_artist") ?: "Unknown"
        val trackId = intent?.getStringExtra("track_id")

        if (!songUrl.isNullOrEmpty()) {
            val metadata = MediaMetadata.Builder()
                .setTitle(songTitle)
                .setArtist(songArtist)
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(songUrl)
                .setMediaId(trackId?:"")
                .setMediaMetadata(metadata)
                .build()

            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true
        }

        return START_NOT_STICKY
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this,     CHANNEL_ID)
            .setContentTitle("On playing")
            .setContentText("SpoTube")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        persistPlaybackState()
        playerNotificationManager?.setPlayer(null)
        mediaSession?.release()
        player?.release()
        playerInstance = null
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "music_channel"
        private const val NOTIFICATION_ID = 1
        private var playerInstance: ExoPlayer? = null
        const val ACTION_STOP_SERVICE = "STOP_MUSIC_SERVICE"
    }

    private class DescriptionAdapter(private val context: Context) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: androidx.media3.common.Player): CharSequence {
            return player.mediaMetadata.title ?: "On playing"
        }

        override fun createCurrentContentIntent(player: androidx.media3.common.Player) = null

        override fun getCurrentContentText(player: androidx.media3.common.Player): CharSequence? {
            return player.mediaMetadata.artist
        }

        override fun getCurrentLargeIcon(
            player: androidx.media3.common.Player,
            callback: PlayerNotificationManager.BitmapCallback
        ) = null
    }

    fun stopMusic() {
        player?.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        persistPlaybackState()
        player?.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun persistPlaybackState() {
        val currentMediaItem = player?.currentMediaItem ?: return
        val trackId = currentMediaItem.mediaId
        val position = player?.currentPosition ?: 0L

        CoroutineScope(Dispatchers.IO).launch {
            try {
                playingRepo.updateUserState(trackId, position.toInt())
                android.util.Log.d("MusicService", "Saved playback state: $trackId at $position ms")
            } catch (e: Exception) {
                android.util.Log.e("MusicService", "Failed to save playback state: ${e.message}")
            }
        }
    }
}
