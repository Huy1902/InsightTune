package com.example.frontend.ui.playingsong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

@Composable
fun PlayerSeekBar(
    playerState: PlayerState,
    onSeek: (Long) -> Unit
) {
    var isUserSeeking by remember {mutableStateOf(false)}
    var currentSliderPosition by remember { mutableStateOf(0L) }

    LaunchedEffect(playerState.currentPosition) {
        if (!isUserSeeking) {
            currentSliderPosition = playerState.currentPosition
        }
    }


    var sliderPosition by remember { mutableStateOf<Float?>(null) }
    val currentPosition = sliderPosition ?: playerState.currentPosition.toFloat()
    val totalDuration = playerState.totalDuration.toFloat().coerceAtLeast(0f)




    Column (modifier = Modifier.fillMaxWidth()) {

        Slider(
            value = currentPosition,
            onValueChange = {
                newPosition -> sliderPosition = newPosition
            },
            valueRange = 0f..(totalDuration),
            onValueChangeFinished = {
                sliderPosition?.toLong()?.let {
                    onSeek(it)
                }
                isUserSeeking = false
                sliderPosition = null

            },

            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color.White.copy(alpha = 0.7f),
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(currentPosition.toLong()), color = Color.White)
            Text(text = formatTime(playerState.totalDuration), color = Color.White)
        }

    }
}

private fun formatTime(millis: Long): String {
    if (millis < 0) return "00:00"
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}