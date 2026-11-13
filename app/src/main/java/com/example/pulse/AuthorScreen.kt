package com.example.pulse

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulse.data.MusicApi
import com.example.pulse.data.Track

@Composable
fun AuthorScreen(
    artistName: String,
    favoriteIds: List<Long>,
    currentTrackId: Long?,
    isPlaying: Boolean,
    onClose: () -> Unit,
    onTrackClick: (Track) -> Unit,
    onFavoriteClick: (Track) -> Unit,
    onImageClick: (Track) -> Unit,
    coverUpdateTrigger: Int = 0
) {
    val Background = Color(0xFF101114)
    val MainText = Color(0xFFF4F3F1)
    val SecondaryText = Color(0xFF9B9CA5)
    val Accent = Color(0xFFFF8057)
    val context = LocalContext.current

    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(artistName) {
        isLoading = true
        try {
            val result = MusicApi.search(artistName).getOrNull() ?: emptyList()
            tracks = result.filter {
                it.artist.equals(artistName, ignoreCase = true)
            }
        } catch (e: Exception) {
            tracks = emptyList()
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 20.dp, top = 8.dp, end = 20.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.ArrowBack, "Назад", tint = MainText)
                }
                Text(
                    text = artistName,
                    color = MainText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(Modifier.width(48.dp))
            }

            Divider(color = Color(0xFF2D2F34), thickness = 1.dp)

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Accent)
                    }
                }
                tracks.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Треки не найдены", color = SecondaryText, fontSize = 16.sp)
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
                                onArtistClick = {},
                                onFavoriteClick = { onFavoriteClick(track) },
                                onImageClick = { onImageClick(track) }
                            )
                        }
                    }
                }
            }
        }
    }
}