package com.example.frontend.ui.favorite

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.frontend.R
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.ui.home.HorizontalSongCard
import com.example.frontend.ui.playingsong.MusicPlayerViewModel
import com.example.frontend.ui.theme.AppTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun FavoriteScreen(
    vm: FavoriteViewModel,
    playerViewModel: MusicPlayerViewModel,
    navController: NavController
) {
    val tracks by vm.uiTracks.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadFavorites()
    }

    FavoriteScreenContent(
        vm,
        playerViewModel,
        tracks = tracks,
        isLoading = isLoading,
        navController = navController
    )
}

@Composable
fun FavoriteScreenContent(
    vm: FavoriteViewModel,
    playerViewModel: MusicPlayerViewModel,
    tracks: List<TrackUiModel>,
    isLoading: Boolean,
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.favorite_songs),
            style = AppTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacing().M)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (tracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.no_favorite_songs),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                    )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = AppTheme.spacing().M)
            ) {
                items(tracks) { track ->
                    FavoriteSongItem(
                        trackModel = track,
                        playerViewModel = playerViewModel,
                        onRemoveClick = { vm.deleteFavorite(track.trackInfo.id) },
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteSongItem(
    trackModel: TrackUiModel,
    playerViewModel: MusicPlayerViewModel,
    onRemoveClick: () -> Unit,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val track = trackModel.trackInfo
        val artistString = track.artists?.joinToString(", ") ?: "Unknown"

        HorizontalSongCard(
            songName = track.title,
            artistName = artistString,
            coverImageUrl = trackModel.coverImageUrl,
            onClick = {
                playerViewModel.playSong(
                    newTrackId = track.id,
                    newUrlKey = track.storageKey,
                    newTitle = track.title,
                    newArtist = artistString,
                    newImageKey = track.coverImageKey ?: "no_image"
                )
                navController.navigate("track_player_screen")
            }
        )
        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Remove from favorites",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

