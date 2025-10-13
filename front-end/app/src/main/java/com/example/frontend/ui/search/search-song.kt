package com.example.frontend.ui.search

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.frontend.ui.theme.AppTheme

data class SearchItem(
    val title: String,
    val subtitle: String,
    val isArtist: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSong(onSwitchSearchSong: () -> Unit) {

    var query by remember { mutableStateOf("") }
    var active by remember { mutableStateOf(false) }

    val recentSearches = listOf(
        SearchItem("FKA twigs", "Artist", isArtist = true),
        SearchItem("Hozier", "Artist", isArtist = true),
        SearchItem("Grimes", "Artist", isArtist = true),
        SearchItem("1 (Remastered)", "Album · The Beatles", isArtist = false),
        SearchItem("HAYES", "Artist", isArtist = true),
        SearchItem("Led Zeppelin", "Artist", isArtist = true),
        SearchItem("Les", "Song · Childish Gambino", isArtist = false)
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppTheme.spacing().M)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    onSearch = { active = false },
                    active = active,
                    onActiveChange = { active = it },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    placeholder = {
                        Text(
                            "Search",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(AppTheme.radius().Small),
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        inputFieldColors = TextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            cursorColor = MaterialTheme.colorScheme.primary
                        )
                    ),
                    tonalElevation = AppTheme.elevation().None
                ) {}

                TextButton(
                    onClick = onSwitchSearchSong,
                    modifier = Modifier.padding(start = AppTheme.spacing().S)
                ) {
                    Text(
                        "Cancel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            Spacer(Modifier.height(AppTheme.spacing().L))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Recent searches",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(AppTheme.spacing().M))
                recentSearches.forEach { item ->
                    RecentSearchItem(item)
                }
            }
        }
    }
}

@Composable
fun RecentSearchItem(item: SearchItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing().S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant) // <-- THAY ĐỔI
                .clip(if (item.isArtist) CircleShape else RoundedCornerShape(AppTheme.radius().Small)) // <-- THAY ĐỔI
        )

        Spacer(Modifier.width(AppTheme.spacing().M)) // <-- THAY ĐỔI

        Column {
            Text(
                item.title,
                color = MaterialTheme.colorScheme.onBackground, // <-- THAY ĐỔİ
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                item.subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // <-- THAY ĐỔI
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(name = "Light Mode", showSystemUi = true)
@Preview(name = "Dark Mode", showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SearchSongPreview() {
    AppTheme {
        SearchSong(onSwitchSearchSong = {})
    }
}