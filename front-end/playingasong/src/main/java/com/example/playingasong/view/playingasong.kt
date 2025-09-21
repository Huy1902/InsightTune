package com.example.playingasong.view

import com.example.playingasong.R
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Playingasong (onSwitchPlayingasong: () -> Unit) {

    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF962419),
            0.45f to Color(0xFF661710),
            1f to Color(0xFF430E09)
        )
    )

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
            Row (
                horizontalArrangement = Arrangement.Center
            ) {
                Box (
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(60.dp),
                    contentAlignment = Alignment.Center

                ) {
                    Image(
                        painter = painterResource(R.drawable.progressbar),
                        contentDescription = null,
                        modifier = Modifier
                            .size(490.dp)
                    )
                }
            }

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
                Image (
                    painter = painterResource(R.drawable.pausesong),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
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

@Preview
@Composable
fun PlayingasongPreview() {
    Playingasong (onSwitchPlayingasong = {})
}