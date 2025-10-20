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
import com.example.frontend.ui.theme.AppTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun FavoriteScreen(
    vm: FavoriteViewModel,
    navController: NavController
) {
    val tracks by vm.uiTracks.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadFavorites()
    }

    FavoriteScreenContent(
        tracks = tracks,
        isLoading = isLoading,
        onRemoveFavorite = { TODO() },
        navController = navController
    )
}

@Composable
fun FavoriteScreenContent(
    tracks: List<TrackUiModel>,
    isLoading: Boolean,
    onRemoveFavorite: (String) -> Unit,
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
                Text(stringResource(R.string.no_favorite_songs))
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = AppTheme.spacing().M)
            ) {
                items(tracks) { track ->
                    FavoriteSongItem(
                        trackModel = track,
                        onRemoveClick = { },
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
    onRemoveClick: () -> Unit,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val track = trackModel.trackInfo
        val artistString = track.artists?.joinToString(", ") ?: "Unknown"

        HorizontalSongCard(
            songName = track.title,
            artistName = artistString,
            coverImageUrl = trackModel.coverImageUrl,
            onClick = {
                val encodedUrlKey =
                    URLEncoder.encode(track.storageKey, StandardCharsets.UTF_8.toString())
                val encodedSongName =
                    URLEncoder.encode(track.title, StandardCharsets.UTF_8.toString())
                val encodedArtistName =
                    URLEncoder.encode(artistString, StandardCharsets.UTF_8.toString())
                val encodedImageKey = URLEncoder.encode(
                    track.coverImageKey ?: "no_image",
                    StandardCharsets.UTF_8.toString()
                )

                val route =
                    "track/${track.id}/$encodedUrlKey/$encodedSongName/$encodedArtistName/$encodedImageKey"
                navController.navigate(route)
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

@Preview(name = "Content Loaded", showSystemUi = true)
@Preview(
    name = "Content Loaded (Dark)",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun FavoriteScreenPreview_Content() {
    val fakeTracks = listOf(
        GetTracksResponse(
            "1",
            "Shape of You",
            listOf("Ed Sheeran"),
            "cover1",
            "storage1",
            12,
            "abcdde"
        ),
        GetTracksResponse(
            "2",
            "Blinding Lights",
            listOf("The Weeknd"),
            "cover2",
            "storage2",
            456,
            "abcdde"
        )
    )
//    AppTheme {
//        FavoriteScreenContent(
//            tracks = fakeTracks,
//            isLoading = false,
//            onRemoveFavorite = {},
//            onSongClick = {}
//        )
//    }
}

@Preview(name = "Empty State", showSystemUi = true)
@Composable
fun FavoriteScreenPreview_Empty() {
    AppTheme {
        FavoriteScreenContent(
            tracks = emptyList(),
            isLoading = false,
            onRemoveFavorite = {},
            navController = NavController(LocalContext.current)
        )
    }
}

@Preview(name = "Loading State", showSystemUi = true)
@Composable
fun FavoriteScreenPreview_Loading() {
    AppTheme {
        FavoriteScreenContent(
            tracks = emptyList(),
            isLoading = true,
            onRemoveFavorite = {},
            navController = NavController(LocalContext.current)
        )
    }
}