package com.example.frontend.ui.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.frontend.R
import com.example.frontend.ui.NavRoutes
import com.example.frontend.ui.profile.ProfileViewModel


@Composable
fun HomeScreen(vm: HomeViewModel, appNavController: NavController) {
    val bottomNavController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                BottomNavigationBar(bottomNavController)
            }
        ) { innerPadding ->
            androidx.navigation.compose.NavHost(
                navController = bottomNavController,
                startDestination = BottomNavItem.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreenContent(vm, appNavController)
                }
                composable(BottomNavItem.Search.route) { /* TODO */ }
                composable(BottomNavItem.Playlist.route) { /* TODO */ }
                composable(BottomNavItem.ChatBot.route) { /* TODO */ }
            }
        }
    }
}

@Composable
fun HomeScreenContent(vm: HomeViewModel, appNavController: NavController) {
    var text by remember { mutableStateOf("") }
    val TAG = "HomeScreenContent"
    val songs = listOf(
        Triple(R.drawable.spotube, "Shape of You", "Ed Sheeran"),
        Triple(R.drawable.spotube, "Blinding Lights", "The Weeknd"),
        Triple(R.drawable.spotube, "Levitating", "Dua Lipa"),
        Triple(R.drawable.spotube, "Peaches", "Justin Bieber")
    )

    val tracks by vm.tracks.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val BASE_FILE_URL = "http://10.0.2.2:4000/files/" // emulator

    LaunchedEffect(Unit) {
        vm.loadTracks(limit = 4)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.spotube_cropped),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(80.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.size(14.dp))

            Text(
                "SpoTube",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                color = Color.Red,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            appNavController.navigate(NavRoutes.Profile.route)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            "Recently played",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color(255, 255, 255),
            modifier = Modifier
                .padding(start = 8.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                items(tracks) { track ->
                    Log.d(TAG, "🎧 Render: ${track.title} (${track.coverImageKey})")
                    val tag = "HomeScreenContent"
                    val imageModel: Any = track.coverImageKey
                        ?.takeIf { it.isNotBlank() }
                        ?.let { BASE_FILE_URL + it }
                        ?: R.drawable.spotube
                            .also { Log.w(tag, "⚠️ ${track.title} không có cover -> dùng placeholder") }

//                    SongCard(
//                        imageRes = track.coverImageKey,
//                        songName = track.title,
//                        artistName = "abc"
//                    )
                }
            }
        }
        Text(
            "Editor's picks",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color(255, 255, 255),
            modifier = Modifier
                .padding(start = 8.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(8.dp)
            ) {
                items(tracks ) { track ->

                    SongCard(
                        imageKey = track.coverImageKey,
                        songName = track.title,
                        artistName = "abc",
                        trackKey = track.storageKey,

                        navController = appNavController
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Search,
        BottomNavItem.Playlist,
        BottomNavItem.ChatBot
    )

    NavigationBar(
        containerColor = Color(0xFF000000)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedTextColor = Color.Red,
                    unselectedTextColor = Color.Gray,
                    selectedIconColor = Color.Red,
                    unselectedIconColor = Color.Gray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val sampleViewModel =
        remember { HomeViewModel(context) }
    HomeScreen(sampleViewModel, navController)
}