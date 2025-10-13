package com.example.frontend.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavItem("home", "Home", Icons.Default.Home)
    object Search : BottomNavItem("search", "Search", Icons.Default.Search)
    object Playlist : BottomNavItem("playlist", "Playlist", Icons.Default.LibraryMusic)
    object ChatBot : BottomNavItem("chatbot", "Chat Bot", Icons.Default.Chat)

    object Profile : BottomNavItem("profile", "Profile", Icons.Default.AccountCircle)
}
