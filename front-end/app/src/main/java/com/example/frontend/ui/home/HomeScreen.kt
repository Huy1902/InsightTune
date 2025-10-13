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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.frontend.R
import com.example.frontend.ui.AppGraph
import com.example.frontend.ui.NavRoutes
import com.example.frontend.ui.profile.ProfileScreen
import com.example.frontend.ui.profile.ProfileViewModel
import com.example.frontend.ui.profile.ProfileViewModelFactory
import com.example.frontend.ui.theme.AppTheme
import com.example.frontend.ui.theme.ThemeSetting


@Composable
fun HomeScreen(
    vm: HomeViewModel,
    appNavController: NavController,
    themeSetting: ThemeSetting,
    onThemeChange: (ThemeSetting) -> Unit
) {
    val bottomNavController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.color().Background)
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
                    HomeScreenContent(
                        vm,
                        appNavController,
                        onProfileClick = {
                            bottomNavController.navigate(BottomNavItem.Profile.route)
                        }
                    )
                }
                composable(BottomNavItem.Search.route) { /* TODO */ }
                composable(BottomNavItem.Playlist.route) { /* TODO */ }
                composable(BottomNavItem.ChatBot.route) { /* TODO */ }
                composable(BottomNavItem.Profile.route) {
                    val vm: ProfileViewModel =
                        viewModel(factory = ProfileViewModelFactory(LocalContext.current))
                    ProfileScreen(
                        vm = vm,
                        onNavigateLogin = {
                            appNavController.navigate(AppGraph.AUTH) {
                                popUpTo(AppGraph.MAIN) {
                                    inclusive = true
                                }
                            }
                        },
                        onBack = { bottomNavController.popBackStack() },
                        themeSetting = themeSetting,
                        onThemeChange = onThemeChange
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    vm: HomeViewModel,
    appNavController: NavController,
    onProfileClick: () -> Unit
) {
    val tracks by vm.tracks.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
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
                fontWeight = FontWeight.Bold,
                style = AppTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(50.dp)
                        .clickable {
                            onProfileClick()
                        }
                )
            }
        }

        Spacer(modifier = Modifier.size(10.dp))

        Text(
            stringResource(R.string.recently_played),
            style = AppTheme.typography.titleMedium,
            modifier = Modifier
                .padding(start = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppTheme.color().Primary)
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                items(tracks) { track ->
                    SongCard(
                        imageRes = track.coverImageKey,
                        songName = track.title,
                        artistName = "abc"
                    )
                }
            }
        }
        Text(
            stringResource(R.string.editor_picks),
            style = AppTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(start = 16.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppTheme.color().Primary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .padding(8.dp)
            ) {
                items(tracks) { track ->
                    SongCard(
                        imageRes = track.coverImageKey,
                        songName = track.title,
                        artistName = "abc"
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
        containerColor = Color.Transparent,
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
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground,
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
    // HomeScreen(sampleViewModel, navController)
}