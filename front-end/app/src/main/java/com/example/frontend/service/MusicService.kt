package com.example.frontend.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.ui.PlayerNotificationManager
import com.example.frontend.R

@UnstableApi
class MusicService : MediaSessionService() {

    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    private var playerNotificationManager: PlayerNotificationManager? = null

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        player = ExoPlayer.Builder(this).build()
        mediaSession = MediaSession.Builder(this, player!!).build()

        playerNotificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            CHANNEL_ID
        )
            .setMediaDescriptionAdapter(DescriptionAdapter(this))
            .setSmallIconResourceId(android.R.drawable.star_on)
            .build()
            .apply {
                setMediaSessionToken(mediaSession!!.sessionCompatToken)
                setPlayer(player)
            }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val songUrl = intent?.getStringExtra("song_url")
        val songTitle = intent?.getStringExtra("song_title") ?: "Unknown"
        val songArtist = intent?.getStringExtra("song_artist") ?: "Unknown"

        if (!songUrl.isNullOrEmpty()) {
            val currentMedia = player?.currentMediaItem
            if (currentMedia == null || currentMedia.mediaId != songUrl) {
                val metadata = MediaMetadata.Builder()
                    .setTitle(songTitle)
                    .setArtist(songArtist)
                    .build()

                val item = MediaItem.Builder()
                    .setUri(songUrl)
                    .setMediaId(songUrl)
                    .setMediaMetadata(metadata)
                    .build()
                player?.setMediaItem(item)
                player?.prepare()
            }
            player?.playWhenReady = true
        }

        return super.onStartCommand(intent, flags, startId)
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
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL_ID = "music_channel"
        private const val NOTIFICATION_ID = 1
    }

    private class DescriptionAdapter(private val context: Context) :
        PlayerNotificationManager.MediaDescriptionAdapter {

        override fun getCurrentContentTitle(player: androidx.media3.common.Player): CharSequence {
            return player.mediaMetadata.title ?: "Đang phát nhạc"
        }

        override fun createCurrentContentIntent(player: androidx.media3.common.Player) =
            null

        override fun getCurrentContentText(player: androidx.media3.common.Player): CharSequence? {
            return player.mediaMetadata.artist
        }

        override fun getCurrentLargeIcon(
            player: androidx.media3.common.Player,
            callback: PlayerNotificationManager.BitmapCallback
        ) = null
    }
}
