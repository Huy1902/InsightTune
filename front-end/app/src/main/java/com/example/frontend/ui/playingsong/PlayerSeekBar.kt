package com.example.frontend.ui.playingsong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.example.frontend.ui.playingsong.PlayerState
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSeekBar(
    playerState: PlayerState,
    onSeek: (Long) -> Unit
) {

    var userSeekPosition by remember { mutableStateOf<Float?>(null) }

    val sliderValue = userSeekPosition ?: playerState.currentPosition.toFloat()
    val totalDuration = playerState.totalDuration.toFloat().coerceAtLeast(0f)

    val sliderValueRange = 0f..totalDuration

    LaunchedEffect(playerState.currentPosition) {
        userSeekPosition?.let { target ->
             userSeekPosition = null
        }
    }



    Column(modifier = Modifier.fillMaxWidth()) {

        val trackHeight: Dp = 3.dp
        val thumbDiameter: Dp = 16.dp
        val activeTrackColor: Color = MaterialTheme.colorScheme.primary
        val inactiveTrackColor: Color = Color(0xFF888888)
        val thumbColor: Color = MaterialTheme.colorScheme.primary

        Slider(
            value = sliderValue,
            onValueChange = { newPosition ->
                userSeekPosition = newPosition
            },
            valueRange = 0f..totalDuration,
            onValueChangeFinished = {
                userSeekPosition?.toLong()?.let { onSeek(it) }

            },
            colors = SliderDefaults.colors(
                thumbColor = Color.Transparent,
                activeTrackColor = Color.Transparent,
                inactiveTrackColor = Color.Transparent
            ),
            track = { sliderPositions ->
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(trackHeight)
                ) {
                    val trackStart = Offset(0f, center.y)
                    val trackEnd = Offset(size.width, center.y)

                    val totalRange = sliderValueRange.endInclusive - sliderValueRange.start
                    val progress = (sliderValue - sliderValueRange.start) / totalRange
                    val fraction = if (totalRange > 0) progress.coerceIn(0f, 1f) else 0f

                    val activeTrackEnd = Offset(
                        x = lerp(trackStart.x, trackEnd.x, fraction),
                        y = center.y
                    )

                    drawLine(
                        color = inactiveTrackColor,
                        start = activeTrackEnd,
                        end = trackEnd,
                        strokeWidth = trackHeight.toPx(),
                        cap = StrokeCap.Round
                    )

                    drawLine(
                        color = activeTrackColor,
                        start = trackStart,
                        end = activeTrackEnd,
                        strokeWidth = trackHeight.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            },
            thumb = {
                Canvas(modifier = Modifier.size(thumbDiameter)) {
                    drawCircle(
                        color = thumbColor,
                        radius = size.minDimension / 2
                    )
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(sliderValue.toLong()),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = formatTime(playerState.totalDuration),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun formatTime(millis: Long): String {
    if (millis < 0) return "00:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}