package com.example.pulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulse.data.Track

@Composable
fun FavoritesScreen(
    tracks: List<Track>,
    currentTrack: Track?,
    isPlaying: Boolean,
    onTrackClick: (Track) -> Unit,
    onRemoveFavorite: (Track) -> Unit,
    onArtistClick: (String) -> Unit,
    onImageClick: (Track) -> Unit,
    coverUpdateTrigger: Int = 0
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF101114))) {
        if (tracks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().statusBarsPadding(), contentAlignment = Alignment.Center) {
                Text("Избранное пусто", color = Color(0xFF9B9CA5), fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(), // Отступ от часов

                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 140.dp)
            ) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Избранное",
                        color = Color(0xFFF4F3F1),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(20.dp))
                }
                items(tracks, key = { "${it.id}_$coverUpdateTrigger" }) { track ->
                    TrackRow(
                        track = track,
                        isPlaying = currentTrack?.id == track.id && isPlaying,
                        isFavorite = true,
                        customCoverPath = CoverManager.getCoverPath(context, track.id),
                        onClick = { onTrackClick(track) },
                        onFavoriteClick = { onRemoveFavorite(track) },
                        onImageClick = { onImageClick(track) },
                        onArtistClick = { onArtistClick(track.artist) }
                    )
                }
            }
        }
    }
}