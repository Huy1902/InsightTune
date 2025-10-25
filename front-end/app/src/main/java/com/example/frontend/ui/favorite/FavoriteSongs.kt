package com.example.frontend.ui.favorite

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.example.frontend.R
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.data.models.song.NextTracksResponse
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AppTheme.spacing().M, end = AppTheme.spacing().M),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { playerViewModel.toggleFavoriteShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle Favorites",
                        tint = if (playerViewModel.isFavoriteShuffleOn())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = AppTheme.spacing().M)
            ) {
                itemsIndexed(tracks) { index, trackUiModel ->
                    FavoriteSongItem(
                        trackModel = trackUiModel,
                        onRemoveClick = { vm.deleteFavorite(trackUiModel.trackInfo.id) },
                        onClick = {
                            val favoritePlaylist =
                                tracks.map { it.trackInfo.toNextTracksResponse() }

                            playerViewModel.setPlaylist(
                                newTracks = favoritePlaylist,
                                startIndex = index,
                                allowFetching = false
                            )
                            navController.navigate("track_player_screen")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteSongItem(
    trackModel: TrackUiModel,
    onRemoveClick: () -> Unit,
    onClick: () -> Unit
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
            onClick = onClick
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

private fun GetTracksResponse.toNextTracksResponse(): NextTracksResponse {
    return NextTracksResponse(
        id = this.id,
        title = this.title,
        artists = this.artists ?: emptyList(),
        albumId = this.albumId,
        storageKey = this.storageKey,
        durationMs = this.durationMs,
        coverImageKey = this.coverImageKey ?: "no_image"
    )
}
