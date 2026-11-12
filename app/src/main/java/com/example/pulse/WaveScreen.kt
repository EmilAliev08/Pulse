package com.example.pulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun WaveScreen(
    tracks: List<Track>,
    isLoading: Boolean,
    onClose: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onArtistClick: (String) -> Unit,
    favoriteIds: List<Long>,
    onFavoriteClick: (Track) -> Unit,
    onImageClick: (Track) -> Unit,
    currentTrackId: Long? = null,
    isPlaying: Boolean = false,
    coverUpdateTrigger: Int = 0
) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF101114))) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.ArrowBack, "Назад", tint = Color.White)
                }
                Spacer(Modifier.width(16.dp))
                Text("Моя волна", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFFF8057))
                    }
                }
                tracks.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Послушайте больше треков, чтобы волна узнала ваш вкус", color = Color(0xFF9B9CA5))
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp)
                    ) {
                        items(tracks, key = { "${it.id}_$coverUpdateTrigger" }) { track ->
                            TrackRow(
                                track = track,
                                isPlaying = currentTrackId == track.id && isPlaying,
                                isFavorite = favoriteIds.contains(track.id),
                                customCoverPath = CoverManager.getCoverPath(context, track.id),
                                onClick = { onTrackClick(track) },
                                onFavoriteClick = { onFavoriteClick(track) },
                                onImageClick = { onImageClick(track) },
                                onArtistClick = { onArtistClick(track.artist) }
                            )
                        }
                    }
                }
            }
        }
    }
}