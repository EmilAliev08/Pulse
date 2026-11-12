package com.example.pulse

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pulse.data.Track

@Composable
fun TrackRow(
    track: Track,
    isPlaying: Boolean,
    isFavorite: Boolean,
    customCoverPath: String? = null,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onImageClick: () -> Unit,
    onArtistClick: (() -> Unit)? = null
) {
    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {

        Surface(
            modifier = Modifier.size(56.dp),
            color = Color(0xFF49324C),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (customCoverPath != null) {
                Image(
                    painter = rememberAsyncImagePainter(customCoverPath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Filled.MusicNote,
                        null,
                        tint = Color(0xFFF4F3F1),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                color = if (isPlaying) Color(0xFFFF8057) else Color(0xFFF4F3F1),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            // Артист — оставляем свой клик (переход на страницу автора)
            Text(
                text = track.artist,
                color = Color(0xFF9B9CA5),
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.clickable { onArtistClick?.invoke() }
            )
        }

        // Кнопки остаются как есть — они перехватывают клики сами
        IconButton(onClick = onImageClick) {
            Icon(
                Icons.Filled.Landscape,
                "Предложить картинку",
                tint = Color(0xFF9B9CA5),
                modifier = Modifier.size(24.dp)
            )
        }
        IconButton(onClick = onFavoriteClick) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Избранное",
                tint = if (isFavorite) Color(0xFFFF8057) else Color(0xFF9B9CA5),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}