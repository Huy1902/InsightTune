import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.frontend.ui.home.BottomNavItem
import com.example.frontend.R

@Composable
fun HomeScreen() {
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFFFF0000),
                        0.3f to Color(0xFF8B0000),
                        0.6f to Color(0xFF000000)
                    )
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomNavigationBar(navController) }

        ) { innerPadding ->
            NavHost(
                navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(BottomNavItem.Home.route) {
                    HomeScreenContent()
                }
                composable(BottomNavItem.Search.route) {

                }
                composable(BottomNavItem.Playlist.route) {

                }
                composable(BottomNavItem.ChatBot.route) {

                }
            }
        }
    }
}

@Composable
fun SongCard(
    imageRes: Int,
    songName: String,
    artistName: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2C2C2C)
        ),
        modifier = modifier
            .width(160.dp)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.spotube),
                contentDescription = songName,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = songName,
                color = Color(255, 255, 255),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )

            Text(
                text = artistName,
                color = Color(255, 255, 255),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}

@Composable
fun HomeScreenContent() {
    var text by remember { mutableStateOf("") }
    val songs = listOf(
        Triple(R.drawable.spotube, "Shape of You", "Ed Sheeran"),
        Triple(R.drawable.spotube, "Blinding Lights", "The Weeknd"),
        Triple(R.drawable.spotube, "Levitating", "Dua Lipa"),
        Triple(R.drawable.spotube, "Peaches", "Justin Bieber")
    )

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            "Search",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Color(255, 255, 255),
            fontSize = 23.sp,
            modifier = Modifier
                .padding(10.dp)
        )

        TextField(
            placeholder = {
                Text(
                    "Search by songs, artists or categories",
                    fontFamily = FontFamily.Serif
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(modifier = Modifier.size(10.dp))

        Row(
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp)
        ) {
            Text(
                "Recently played",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Color(255, 255, 255)
            )

            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = Color(255, 255, 255)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color(255, 255, 255)
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            items(songs) { (img, name, artist) ->
                SongCard(
                    imageRes = img,
                    songName = name,
                    artistName = artist
                )
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(8.dp)
        ) {
            items(songs) { (img, name, artist) ->
                SongCard(
                    imageRes = img,
                    songName = name,
                    artistName = artist
                )
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
    HomeScreen()
}
