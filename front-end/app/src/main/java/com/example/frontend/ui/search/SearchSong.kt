package com.example.frontend.ui.search

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.frontend.R
import com.example.frontend.data.models.home.GetTracksResponse
import com.example.frontend.data.models.search.SearchHistoryResponse
import com.example.frontend.ui.home.HorizontalSongCard
import com.example.frontend.ui.home.SongCard
import com.example.frontend.ui.home.SongCardLayout
import com.example.frontend.ui.playingsong.MusicPlayerViewModel
import com.example.frontend.ui.theme.AppTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SearchScreen(
    vm: SearchViewModel,
    playerViewModel: MusicPlayerViewModel,
    navController: NavController,
    onCancel: () -> Unit
) {
    val query by vm.searchQuery.collectAsState()
    val searchResults by vm.searchResults.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val recentSearches by vm.recentSearches.collectAsState()

    SearchScreenContent(
        query = query,
        onQueryChange = vm::onQueryChange,
        searchResults = searchResults,
        isLoading = isLoading,
        recentSearches = recentSearches,
        onSearch = { keyword ->
            vm.onQueryChange(keyword)
            vm.loadRecentSearches(10)
        },
        navController = navController,
        playerViewModel,
        onCancel = onCancel
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreenContent(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: List<TrackUiModel>,
    isLoading: Boolean,
    recentSearches: List<SearchHistoryResponse>,
    onSearch: (String) -> Unit,
    navController: NavController,
    playerViewModel: MusicPlayerViewModel,
    onCancel: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = AppTheme.spacing().M, end = AppTheme.spacing().M),
        verticalArrangement = Arrangement.Top
    ) {
        DockedSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = {
                Log.d("SEARCH_DEBUG", "1. UI Layer received: $query")
                onSearch(query)
            },
            active = false,
            onActiveChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = SearchBarDefaults.dockedShape
                ),
            placeholder = { Text(stringResource(R.string.search_title)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            },
            colors = SearchBarDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        ) {}

//        DockedSearchBar(
//            query = query,
//            onQueryChange = onQueryChange,
//            onSearch = {
//                Log.d("SEARCH_DEBUG", "1. UI Layer received: $query")
//                onSearch(query)
//            },
//            active = false,
//            onActiveChange = { },
//            modifier = Modifier.fillMaxWidth(),
//            placeholder = { Text(stringResource(R.string.search_title)) },
//            leadingIcon = {
//                Icon(Icons.Default.Search, contentDescription = null)
//            },
//            trailingIcon = {
//                if (query.isNotEmpty()) {
//                    IconButton(onClick = { onQueryChange("") }) {
//                        Icon(
//                            Icons.Default.Close,
//                            contentDescription = stringResource(R.string.close)
//                        )
//                    }
//                }
//            },
//            colors = SearchBarDefaults.colors(
//                containerColor = MaterialTheme.colorScheme.surfaceVariant
//            )
//        ) {}

        Spacer(Modifier.height(AppTheme.spacing().L))


        if (query.isNotBlank()) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(AppTheme.spacing().S)
                ) {
                    items(searchResults) { trackModel ->
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
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.no_result),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        } else {
            Text(
                stringResource(R.string.recent_search),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(AppTheme.spacing().M))
            LazyColumn {
                items(recentSearches) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onQueryChange(item.search) }
                            .padding(vertical = AppTheme.spacing().M),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "History",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(AppTheme.spacing().M))
                        Text(
                            item.search,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showSystemUi = true)
@Preview(name = "Dark Mode", showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SearchScreenPreview() {
    val fakeTracks = listOf(
        GetTracksResponse(
            "1",
            "Shape of You",
            listOf("Ed Sheeran"),
            albumId = "123",
            coverImageKey = "123",
            durationMs = 1234,
            storageKey = "123"
        ),
        GetTracksResponse(
            "2",
            "Blinding Lights",
            listOf("The Weeknd"),
            albumId = "123",
            coverImageKey = "123",
            durationMs = 1234,
            storageKey = "123"
        )
    )
//    AppTheme {
//        SearchScreenContent(
//            query = "test",
//            onQueryChange = {},
//            searchResults = fakeTracks,
//            isLoading = false,
//            onSearch = {},
//            recentSearches = { emptyList() },
//            onCancel = {}
//        )
//    }
}
