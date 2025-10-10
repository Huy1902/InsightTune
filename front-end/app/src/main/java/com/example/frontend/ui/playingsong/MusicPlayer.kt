package com.example.frontend.ui.playingsong
import PlayerSeekBar
import android.R.attr.onClick
import androidx.compose.foundation.Image
import com.example.frontend.R
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun MusicPlayer(
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit
) {

    val gradient = Brush.verticalGradient(
        colorStops = arrayOf(
            0.0f to Color(0xFF962419),
            0.45f to Color(0xFF661710),
            1f to Color(0xFF430E09)
        )
    )

    val state by viewModel.playerState.collectAsState()
    val currentTrack = state.currentTrack

    if (currentTrack != null) {
        return
    }
//    val songUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp, 0.dp)
//                .statusBarsPadding()
                .systemBarsPadding()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row (modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .size(25.dp)
                        .rotate(270f)
                        .clickable {
                            onBack()
                        },
                    tint = Color.White
                )
                Text(
                    text = "1 (Remastered)",
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Image(
                    painter = painterResource(R.drawable.dots),
                    contentDescription = "Option",
                )
            }
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.Center, // cách đều ảnh
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage (
                    model = viewModel.getImage(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }

            Row (
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = viewModel.getTitle(),
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
                    text = viewModel.getArtist(),
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
                IconButton(onClick =  {}) {
                    Image(
                        painter = painterResource(R.drawable.shuffle),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = { /*TODO*/ }) {
                    Image(
                        painter = painterResource(R.drawable.previoussong),
                        contentDescription = null,
                        modifier = Modifier.size(30.dp)
                    )
                }
                IconButton (onClick = {viewModel.onPlayPauseClick() }) {
                    Image(
                        painter = painterResource(if (state.isPlaying) R.drawable.pausesong else R.drawable.playing),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                }
                IconButton(onClick = {viewModel.onPlayNextSong() } ) {
                    Image(
                        painter = painterResource(R.drawable.nextsong),
                        contentDescription = null,
                        modifier = Modifier.size(23.dp)
                    )
                }
                IconButton(onClick = {}) {
                    Image(
                        painter = painterResource(R.drawable.like),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        alignment = Alignment.CenterEnd
                    )
                }
            }
        }

    }
}

