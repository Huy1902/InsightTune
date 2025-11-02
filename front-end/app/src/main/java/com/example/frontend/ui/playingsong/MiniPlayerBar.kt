package com.example.frontend.ui.playingsong

import android.R
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.util.concurrent.TimeUnit

@Composable
fun MiniPlayerBar(
    viewModel: MusicPlayerViewModel,
    onExpandPlayer: () -> Unit
) {
    val playerState by viewModel.playerState.collectAsState()

    if (playerState.mediaMetadata.title == null) {
        return
    }

    val currentPos = playerState.currentPosition
    val totalDur = playerState.totalDuration.coerceAtLeast(1L)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandPlayer() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = rememberAsyncImagePainter(playerState.coverImageUrl ?: ""),
                contentDescription = "Cover",
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = playerState.mediaMetadata.title?.toString() ?: "On Playing",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                Text(
                    text = playerState.mediaMetadata.artist?.toString() ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            IconButton(
                onClick = { viewModel.playPreviousTrack() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = { viewModel.onPlayPauseClick() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            IconButton(
                onClick = { viewModel.playNextTrack() },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        val activeTrackColor = MaterialTheme.colorScheme.primary
        val inactiveTrackColor = Color.Gray.copy(alpha = 0.4f)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .padding(top = 4.dp)
        ) {
            val progress = (currentPos.toFloat() / totalDur.toFloat()).coerceIn(0f, 1f)

            val trackStart = Offset(0f, center.y)
            val trackEnd = Offset(size.width, center.y)
            val activeEnd = Offset(size.width * progress, center.y)

            drawLine(
                color = inactiveTrackColor,
                start = activeEnd,
                end = trackEnd,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            drawLine(
                color = activeTrackColor,
                start = trackStart,
                end = activeEnd,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 2.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = formatTime(currentPos),
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray
//            )
//            Text(
//                text = formatTime(totalDur),
//                style = MaterialTheme.typography.bodyLarge,
//                color = Color.Gray
//            )
//        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis <= 0) return "00:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}