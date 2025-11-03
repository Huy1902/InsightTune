package com.example.frontend.ui.home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.frontend.R
import com.example.frontend.core.AppPreferences
import com.example.frontend.data.models.home.GetTracksResponse
import com.example.frontend.data.models.playingsong.NextTracksResponse
import com.example.frontend.data.remote.ApiClient
import com.example.frontend.data.remote.FavoriteRepositoryImpl
import com.example.frontend.data.remote.HistoryRepositoryImpl
import com.example.frontend.data.remote.PlayingRepositoryImpl
import com.example.frontend.data.remote.TrackRepositoryImpl
import com.example.frontend.ui.AppGraph
import com.example.frontend.ui.chatbot.ChatBotScreen
import com.example.frontend.ui.chatbot.ChatbotViewModel
import com.example.frontend.ui.chatbot.ChatbotViewModelFactory
import com.example.frontend.ui.favorite.FavoriteScreen
import com.example.frontend.ui.favorite.FavoriteViewModel
import com.example.frontend.ui.favorite.FavoriteViewModelFactory
import com.example.frontend.ui.playingsong.MiniPlayerBar
import com.example.frontend.ui.playingsong.MusicPlayer
import com.example.frontend.ui.playingsong.MusicPlayerViewModel
import com.example.frontend.ui.playingsong.MusicPlayerViewModelFactory
import com.example.frontend.ui.profile.ProfileScreen
import com.example.frontend.ui.profile.ProfileViewModel
import com.example.frontend.ui.profile.ProfileViewModelFactory
import com.example.frontend.ui.search.SearchScreen
import com.example.frontend.ui.search.SearchViewModel
import com.example.frontend.ui.search.SearchViewModelFactory
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

    val context = LocalContext.current
    val favoriteRepo = FavoriteRepositoryImpl(ApiClient.favoriteApi)
    val playingRepo = PlayingRepositoryImpl(ApiClient.playingApi)
    val prefs = AppPreferences(LocalContext.current)
    val historyRepository = HistoryRepositoryImpl(ApiClient.historyApi, prefs)

    val playerViewModel: MusicPlayerViewModel = viewModel(
        factory = MusicPlayerViewModelFactory(
            favoriteRepo = favoriteRepo,
            playingRepo = playingRepo,
            historyRepo = historyRepository,
            context = context
        )
    )
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showMiniPlayer =
        currentRoute != BottomNavItem.Track.route && currentRoute != "track_player_screen"
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Column {
                if (showMiniPlayer) {
                    MiniPlayerBar(
                        viewModel = playerViewModel,
                        onExpandPlayer = {
                            bottomNavController.navigate("track_player_screen")
                        }
                    )
                }
                BottomNavigationBar(bottomNavController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {

                val playingVmFactory: PlayingViewModelFactory =
                    PlayingViewModelFactory(LocalContext.current)
                val playingVm: PlayingViewModel = viewModel(factory = playingVmFactory)

                HomeScreenContent(
                    vm,
                    playerViewModel,
                    appNavController,
                    onProfileClick = {
                        bottomNavController.navigate(BottomNavItem.Profile.route)
                    },
                    bottomNavController
                )
            }
            composable(BottomNavItem.Search.route) {
                val trackRepository = TrackRepositoryImpl(ApiClient.trackApi)
                val searchViewModelFactory =
                    SearchViewModelFactory(trackRepository, historyRepository, prefs)

                val vm: SearchViewModel = viewModel(factory = searchViewModelFactory)

                SearchScreen(
                    vm = vm,
                    playerViewModel,
                    navController = bottomNavController,
                    onCancel = { bottomNavController.popBackStack() }
                )
            }
            composable(BottomNavItem.Favorites.route) {
                val favoriteRepo = FavoriteRepositoryImpl(ApiClient.favoriteApi)
                val playingRepo = PlayingRepositoryImpl(ApiClient.playingApi)

                val favoriteViewModelFactory =
                    FavoriteViewModelFactory(favoriteRepo, playingRepo)
                val vm: FavoriteViewModel = viewModel(factory = favoriteViewModelFactory)
                FavoriteScreen(
                    vm,
                    playerViewModel,
                    bottomNavController
                )
            }
            composable(BottomNavItem.ChatBot.route) {
                val chatbotApi = ApiClient.chatbotApi
                val trackRepository = TrackRepositoryImpl(ApiClient.trackApi)
                val chatbotViewModelFactory = ChatbotViewModelFactory(chatbotApi, trackRepository)
                val vm: ChatbotViewModel = viewModel(factory = chatbotViewModelFactory)
                ChatBotScreen(
                    vm,
                    musicPlayerViewModel = playerViewModel,
                    appNavController
                )
            }
            composable(
                BottomNavItem.Track.route,
                arguments = listOf(
                    navArgument("urlKey") { type = NavType.StringType },
                    navArgument("title") { type = NavType.StringType },
                    navArgument("artist") { type = NavType.StringType },
                    navArgument("imageKey") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val context = LocalContext.current
                val trackId = backStackEntry.arguments?.getString("trackId") ?: ""
                val storageKey = backStackEntry.arguments?.getString("urlKey") ?: ""
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val artistStr = backStackEntry.arguments?.getString("artist") ?: ""
                val artists = artistStr.split(",").map { it.trim() }
                val albumId = backStackEntry.arguments?.getString("durationMs") ?: ""
                val durationMs = backStackEntry.arguments?.getLong("durationMs") ?: 0
                val coverImageKey = backStackEntry.arguments?.getString("coverImageKey") ?: ""
//                val url = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
//                val title = URLDecoder.decode(encodedTitle, StandardCharsets.UTF_8.toString())
//                val artist = URLDecoder.decode(encodedArtist, StandardCharsets.UTF_8.toString())
//                val imageUrl = URLDecoder.decode(encodedImageUrl, StandardCharsets.UTF_8.toString())

                val favoriteRepo = FavoriteRepositoryImpl(ApiClient.favoriteApi)
                val playingRepo = PlayingRepositoryImpl(ApiClient.playingApi)

                val singleTrackList = listOf(
                    NextTracksResponse(
                        id = trackId,
                        title = title,
                        artists = artists,
                        albumId = albumId,
                        storageKey = storageKey,
                        durationMs = durationMs,
                        coverImageKey = coverImageKey
                    )
                )

                val vm: MusicPlayerViewModel = viewModel(
                    factory = MusicPlayerViewModelFactory(
                        trackList = singleTrackList,
                        favoriteRepo = favoriteRepo,
                        playingRepo = playingRepo,
                        historyRepo = historyRepository,
                        context = context
                    )
                )
                MusicPlayer(
                    viewModel = vm,
                    onBack = { bottomNavController.popBackStack() }
                )
            }

            composable("track_player_screen") {
                MusicPlayer(
                    viewModel = playerViewModel,
                    onBack = { bottomNavController.popBackStack() }
                )
            }

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

@Composable
fun HomeScreenContent(
    vm: HomeViewModel,
    playerViewModel: MusicPlayerViewModel,
    appNavController: NavController,
    onProfileClick: () -> Unit,
    bottomNavController: NavController
) {
    val tracks by vm.tracks.collectAsState()
    val uiTracks by vm.uiTracks.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val history by vm.history.collectAsState()
    val recommendTracks by vm.uiRecommendTracks.collectAsState()

    LaunchedEffect(Unit) {
        vm.loadRecommendTracks(limit = 5)
    }

    LaunchedEffect(Unit) {
        vm.loadTracks()
    }

    LaunchedEffect(Unit) {
        vm.loadHistory(limit = 8)
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        //horizontalAlignment = Alignment.Start
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

            Box(
                modifier = Modifier.height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "SpoTube",
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.offset(y = (-3).dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(
                onClick = { onProfileClick() },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                item {
                    Text(
                        "Made for you",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        style = AppTheme.typography.bodyLarge,
                        modifier = Modifier
                            .padding(start = 16.dp)
                    )
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        itemsIndexed(recommendTracks) { index, trackUiModel ->
                            val track = trackUiModel.trackInfo
                            val artistString = track.artists?.joinToString(", ") ?: "Unknown"
                            SongCard(
                                songName = track.title,
                                artistName = artistString,
                                coverImageUrl = trackUiModel.coverImageUrl,
                                onClick = {
                                    playerViewModel.playSong(
                                        newTrackId = track.id,
                                        newUrlKey = track.storageKey,
                                        newTitle = track.title,
                                        newArtist = artistString,
                                        newImageKey = track.coverImageKey ?: "no_image"
                                    )
                                    bottomNavController.navigate("track_player_screen")
                                }
                            )
                        }
                    }
                }
                item {
                    if (history.isNotEmpty()) {
                        Text(
                            stringResource(R.string.recently_played),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            style = AppTheme.typography.bodyLarge,
                            modifier = Modifier
                                .padding(start = 16.dp)
                        )
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            itemsIndexed(history) { index, trackUiModel ->
                                val track = trackUiModel.trackInfo
                                val artistString = track.artists?.joinToString(", ") ?: "Unknown"
                                Log.d(
                                    "HistoryDebug",
                                    "title=${track.title}, artist=$artistString, image=${trackUiModel.coverImageUrl}"
                                )
                                SongCard(
                                    songName = track.title,
                                    artistName = artistString,
                                    coverImageUrl = trackUiModel.coverImageUrl,
                                    onClick = {
                                        playerViewModel.playSong(
                                            newTrackId = track.id,
                                            newUrlKey = track.storageKey,
                                            newTitle = track.title,
                                            newArtist = artistString,
                                            newImageKey = track.coverImageKey ?: "no_image"
                                        )
                                        bottomNavController.navigate("track_player_screen")
                                    }
                                )
                            }
                        }
                    }
                }
                item {
                    Text(
                        stringResource(R.string.editor_picks),
                        style = AppTheme.typography.bodyLarge,
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
                            // CircularProgressIndicator(color = AppTheme.color().Primary)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .heightIn(max = 800.dp)
                                .padding(8.dp)
                        ) {
                            items(uiTracks) { trackUiModel ->
                                val track = trackUiModel.trackInfo
                                val artistString = track.artists?.joinToString(", ") ?: "Unknown"

                                SongCard(
                                    songName = track.title,
                                    artistName = artistString,
                                    coverImageUrl = trackUiModel.coverImageUrl,
                                    onClick = {
                                        playerViewModel.playSong(
                                            newTrackId = track.id,
                                            newUrlKey = track.storageKey,
                                            newTitle = track.title,
                                            newArtist = artistString,
                                            newImageKey = track.coverImageKey ?: "no_image"
                                        )
                                        bottomNavController.navigate("track_player_screen")
                                    }
                                )
                            }
                        }
                    }
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
        BottomNavItem.Favorites,
        BottomNavItem.ChatBot
    )

    NavigationBar(
        containerColor = Color.Transparent,
        modifier = Modifier.height(80.dp)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            item.icon ?: Icons.Default.AccountCircle,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(stringResource(id = item.labelResId ?: R.drawable.spotube))
                    },
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

// Hàm chuyển đổi Model
fun GetTracksResponse.toNextTracksResponse(): NextTracksResponse {
    return NextTracksResponse(
        id = this.id,
        title = this.title,
        artists = this.artists ?: emptyList(),
        albumId = this.albumId,
        storageKey = this.storageKey,
        durationMs = this.durationMs,
        coverImageKey = this.coverImageKey ?: ""
    )
}