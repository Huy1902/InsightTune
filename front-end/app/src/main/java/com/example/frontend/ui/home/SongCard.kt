package com.example.frontend.ui.home

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend.R

@Composable
fun SongCard(
    imageRes: String?,     // 👈 URL có thể null
    songName: String,
    artistName: String,
    urlTrack: String,
    modifier: Modifier = Modifier,
    navController: NavController
) {
//    imageRes = if null
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2C)),
        modifier = modifier
            .width(160.dp)
            .padding(8.dp)
            .clickable(onClick = {
                val encodedUrl = Uri.encode(urlTrack)
                val encodedTitle = Uri.encode(songName)
                val encodedArtist = Uri.encode(artistName)
                val encodedImageUrl = Uri.encode(imageRes)
                navController.navigate("track/$encodedUrl/$encodedTitle/$encodedArtist/$encodedImageUrl")
            })
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageRes ?: R.drawable.spotube)   // 👈 fallback nếu null
                    .crossfade(true)
                    .build(),
                contentDescription = "Cover of $songName",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = songName,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = artistName,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
