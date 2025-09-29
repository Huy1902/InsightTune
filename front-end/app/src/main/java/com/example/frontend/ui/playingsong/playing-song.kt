package com.example.playingasong.view

import android.widget.SeekBar
import androidx.compose.material3.Slider
import androidx.compose.foundation.Image
import com.example.frontend.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.frontend.ui.playingsong.MusicPlayerViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.example.app.ui.NavRoutes
import com.example.frontend.ui.playingsong.PlayerSeekBar
import com.example.frontend.ui.playingsong.PlayerState
import com.example.frontend.ui.theme.FrontEndTheme

@Composable
fun Playingasong (viewModel: MusicPlayerViewModel) {

    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF962419),
            0.45f to Color(0xFF661710),
            1f to Color(0xFF430E09)
        )
    )

    val state by viewModel.playerState.collectAsState()

    val songUrl = "https://d6puu73zzo17e.cloudfront.net/tracks/5927151815778178627.mp3?Expires=1759119755&Signature=xwiGfQDjQLxIY5XT2F5JgL8vIbFtoejkB4UDDIs76f67-lmYQr5wKKY3nCOWpnXP9nAkzNqTQII0AHuI0jGCTZYKjNLhsZWruQF6HAzbd3ZKf-XDZDJZQuHPKZtw3rAhMmdHRXiqGgCT-P5fEgxAb3zf8IMyZPylUcdRwDiOw6t38hWJmwdVEyENkZJd6biz0brdNp1JHQ-Pydg2IB-gOkwojUFiBsVJdggx0jtkZI-roLFAXXdDsMsm6CBFflwKdrngBPpTR9mgD5A5Nmb7GCm0lZlEHjgUloJ5-tUHadlthAkuUrBwT-UCcZ~~O~OoWb~cCMHKnbpxLyJaqMplXQ__&Key-Pair-Id=K1W74YMJ2QWWV5"
    LaunchedEffect(key1 = songUrl) {
        viewModel.loadAndPlaySong(songUrl)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp, 0.dp)
        ) {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .size(100.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                   painter = painterResource(R.drawable.arrowdown),
                   contentDescription = null,
                   modifier = Modifier
                       .size(24.dp)
                )
                Text(
                    text = "1 (Remastered)",
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Image(
                    painter = painterResource(R.drawable.dots),
                    contentDescription = null,
                )
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center, // cách đều ảnh
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image (
                    painter = painterResource(R.drawable.imagesong),
                    contentDescription = null,
                    modifier = Modifier.size(380.dp)
                )
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "From Me to You - Mono/Remast",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp, 40.dp, 12.dp, 0.dp),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White

                )
            }
            Row () {
                Text(
                    text = "The Beatles",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp, 6.dp),
                    color = Color.White,

                )
            }
            PlayerSeekBar(
                playerState = state,
                onSeek = {
                    newPosition -> viewModel.seekToPosition(newPosition)
                }
            )

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp, 0.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image (
                    painter = painterResource(R.drawable.shuffle),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Image (
                    painter = painterResource(R.drawable.previoussong),
                    contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
                IconButton (onClick = {viewModel.onPlayPauseClick() }) {
                    Image (
                        painter = painterResource(if (state.isPlaying) R.drawable.pausesong else R.drawable.playing),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
        }
                Image (
                    painter = painterResource(R.drawable.nextsong),
                    contentDescription = null,
                    modifier = Modifier.size(23.dp)
                )
                Image (
                    painter = painterResource(R.drawable.like),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    alignment = Alignment.CenterEnd
                )
            }
        }

    }
}

