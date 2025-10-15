package com.example.frontend.ui.home

import androidx.annotation.StringRes // Thêm import này
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.frontend.R

sealed class BottomNavItem(val route: String, @StringRes val labelResId: Int, val icon: ImageVector) {
    object Home : BottomNavItem(
        "home",
        R.string.home,
        Icons.Default.Home
    )

    object Search : BottomNavItem("search", R.string.search, Icons.Default.Search)
    object Playlist : BottomNavItem("playlist", R.string.playlist, Icons.Default.LibraryMusic)
    object ChatBot : BottomNavItem("chatbot", R.string.chat_bot, Icons.Default.Chat)
    object Profile : BottomNavItem("profile", R.string.profile, Icons.Default.AccountCircle)
}