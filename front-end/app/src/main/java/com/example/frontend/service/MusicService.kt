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

@UnstableApi
class MusicService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var playerNotificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        if (playerInstance == null) {
            playerInstance = ExoPlayer.Builder(this)
                .build()
                .apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(C.USAGE_MEDIA)
                            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                            .build(),
                        true
                    )
                    setHandleAudioBecomingNoisy(true)
                }
        }

        player = playerInstance

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
        startForeground(NOTIFICATION_ID, buildNotification())

        val songUrl = intent?.getStringExtra("song_url")
        val songTitle = intent?.getStringExtra("song_title") ?: "Unknown"
        val songArtist = intent?.getStringExtra("song_artist") ?: "Unknown"

        if (!songUrl.isNullOrEmpty()) {
            val current = player?.currentMediaItem

            if (current == null || current.mediaId != songUrl) {
                val metadata = MediaMetadata.Builder()
                    .setTitle(songTitle)
                    .setArtist(songArtist)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(songUrl)
                    .setMediaId(songUrl)
                    .setMediaMetadata(metadata)
                    .build()

                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.playWhenReady = true
            } else {
                // 🔹 Nếu đang pause thì resume thôi
                if (player?.isPlaying == false) {
                    player?.play()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }



    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Đang phát nhạc")
            .setContentText("Ứng dụng SpoTube")
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
    }

    private class DescriptionAdapter(private val context: Context) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: androidx.media3.common.Player): CharSequence {
            return player.mediaMetadata.title ?: "Đang phát nhạc"
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
}
