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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.example.frontend.R
import com.example.frontend.data.models.song.GetTracksResponse
import com.example.frontend.ui.home.HorizontalSongCard
import com.example.frontend.ui.theme.AppTheme

@Composable
fun FavoriteScreen(
    vm: FavoriteViewModel,
    onSongClick: (GetTracksResponse) -> Unit
) {
 //   val tracks by vm.favoriteTracks.collectAsState()
  //  val isLoading by vm.isLoading.collectAsState()

    LaunchedEffect(Unit) {
    //    vm.loadFavorites()
    }

//    FavoriteScreenContent(
//        tracks = tracks,
//        isLoading = isLoading,
//        onRemoveFavorite = { trackId -> vm.removeFavorite(trackId) },
//        onSongClick = onSongClick
//    )
}

@Composable
fun FavoriteScreenContent(
    tracks: List<GetTracksResponse>,
    isLoading: Boolean,
    onRemoveFavorite: (String) -> Unit,
    onSongClick: (GetTracksResponse) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Text(
                text = stringResource(R.string.favorite_songs),
                style = AppTheme.typography.titleLarge,
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
                            track = track,
                            onRemoveClick = { onRemoveFavorite(track.id) },
                            onSongClick = { onSongClick(track) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteSongItem(
    track: GetTracksResponse,
    onRemoveClick: () -> Unit,
    onSongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSongClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
//        HorizontalSongCard(
//            imageRes = track.coverImageKey,
//            songName = track.title,
//            artistName = track.artists?.joinToString(", ") ?: "Unknown Artist",
//            modifier = Modifier.weight(1f)
//        )
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
@Preview(name = "Content Loaded (Dark)", showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FavoriteScreenPreview_Content() {
    val fakeTracks = listOf(
        GetTracksResponse("1", "Shape of You", listOf("Ed Sheeran"), "cover1", "storage1", 12, "abcdde"),
        GetTracksResponse("2", "Blinding Lights", listOf("The Weeknd"), "cover2", "storage2", 456, "abcdde")
    )
    AppTheme {
        FavoriteScreenContent(
            tracks = fakeTracks,
            isLoading = false,
            onRemoveFavorite = {},
            onSongClick = {}
        )
    }
}

@Preview(name = "Empty State", showSystemUi = true)
@Composable
fun FavoriteScreenPreview_Empty() {
    AppTheme {
        FavoriteScreenContent(
            tracks = emptyList(),
            isLoading = false,
            onRemoveFavorite = {},
            onSongClick = {}
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
            onSongClick = {}
        )
    }
}