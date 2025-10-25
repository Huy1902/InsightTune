package com.example.frontend.ui.home

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.frontend.R
import com.example.frontend.ui.theme.AppTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

enum class SongCardLayout {
    VERTICAL,
    HORIZONTAL
}

@Composable
fun SongCard(
    songName: String,
    artistName: String,
    coverImageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .width(160.dp)
            .padding(8.dp)
            .clickable(onClick = {
                onClick()
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
                    .data(coverImageUrl ?: R.drawable.spotube)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(id = R.drawable.spotube),
                contentDescription = "Cover of $songName",
                modifier = Modifier
                    .size(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = songName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = artistName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HorizontalSongCard(
    songName: String,
    artistName: String,
    coverImageUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(vertical = AppTheme.spacing().S)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onClick() }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(coverImageUrl ?: R.drawable.spotube)
                .crossfade(true)
                .build(),
            placeholder = painterResource(id = R.drawable.spotube),
            contentDescription = "Cover of $songName",
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(AppTheme.spacing().M))

        Column {
            Text(
                text = songName,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = artistName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}