package com.example.frontend.ui.playingsong

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.data.models.home.GetTracksResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.domain.repositories.TrackRepository
import com.example.frontend.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Composable
fun MusicPlayer(
    viewModel: MusicPlayerViewModel,
    onBack: () -> Unit
) {

    val state by viewModel.playerState.collectAsState()
    val isRepeatOne by viewModel.isRepeatOne.collectAsState()

//    val currentTrack = state.currentTrack
//    if (currentTrack != null) {
//        return
//    }
//    val songUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "InsightTune",
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "MoreOption",
                    modifier = Modifier.padding(end = 16.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(4f)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = state.coverImageUrl ?: R.drawable.spotube_cropped,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = painterResource(id = R.drawable.spotube_cropped),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f)
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.mediaMetadata.title?.toString() ?: "Unknown Title",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = state.mediaMetadata.artist?.toString() ?: "Unknown Artist",
                    modifier = Modifier.padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                PlayerSeekBar(
                    playerState = state,
                    onSeek = { newPosition -> viewModel.seekToPosition(newPosition) }
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.size(28.dp),
                    onClick = { viewModel.toggleRepeatOne() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Repeat One",
                        tint = if (isRepeatOne) MaterialTheme.colorScheme.onBackground
                        else MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(
                    modifier = Modifier.size(56.dp),
                    onClick = { viewModel.playPreviousTrack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Previous Song",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { viewModel.onPlayPauseClick() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.playNextTrack() },
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Next Song",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    modifier = Modifier.size(28.dp),
                    onClick = {
                        viewModel.onToggleFavorite()
                    }
                ) {
                    Icon(
                        imageVector = if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = "Like",
                        tint = if (state.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Spacer(modifier = Modifier.size(AppTheme.spacing().M))
        }
    }
}
