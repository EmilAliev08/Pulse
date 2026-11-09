package com.example.pulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulse.data.AppDatabase
import com.example.pulse.data.Track
import kotlinx.coroutines.launch
import com.example.pulse.data.toTrack

@Composable
fun FavoritesScreen(
    favoriteIds: List<Long>,
    onTrackClick: (Track) -> Unit,
    onRemoveFavorite: (Track) -> Unit
) {
    val Background = Color(0xFF101114)
    val Accent = Color(0xFFFF8057)
    val MainText = Color(0xFFF4F3F1)
    val SecondaryText = Color(0xFF9B9CA5)

    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()

    val favoriteEntities by db.favoriteDao().getAllFavorites()
        .collectAsState(initial = emptyList())

    val favoriteTracks = favoriteEntities.map { it.toTrack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(40.dp))
        Text(
            text = "Избранное",
            color = MainText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${favoriteTracks.size} треков",
            color = SecondaryText,
            fontSize = 16.sp
        )
        Spacer(Modifier.height(24.dp))

        if (favoriteTracks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        tint = SecondaryText,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Здесь появятся избранные треки",
                        color = SecondaryText,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Нажми ❤️ на треке, чтобы добавить",
                        color = SecondaryText,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(favoriteTracks) { track ->
                    TrackRow(
                        track = track,
                        isPlaying = false,
                        isFavorite = true,
                        onClick = { onTrackClick(track) },
                        onFavoriteClick = {
                            scope.launch {
                                db.favoriteDao().remove(track.id)
                                onRemoveFavorite(track)
                            }
                        },
                        onImageClick = { }
                    )
                }
            }
        }
    }
}