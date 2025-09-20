package com.example.frontend.ui.search

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
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
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray) },
                placeholder = { Text("Search", color = Color.LightGray) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = Color.LightGray)
                        }
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = SearchBarDefaults.colors(
                    containerColor = Color(0xFF2A2A2A),
                    inputFieldColors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White
                    )
                ),
                tonalElevation = 0.dp
            ) {}

            TextButton(
                onClick = onSwitchSearchSong,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    "Cancel",
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Recent searches",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))
            recentSearches.forEach { item ->
                RecentSearchItem(item)
            }
        }
    }
}

@Composable
fun RecentSearchItem(item: SearchItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.DarkGray)
                .clip(if (item.isArtist) CircleShape else RoundedCornerShape(4.dp))
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                item.title,
                color = Color.White,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                item.subtitle,
                color = Color.Gray,
                fontSize = 14.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun SearchSongPreview() {
    MaterialTheme {
        SearchSong(onSwitchSearchSong = {})
    }
}