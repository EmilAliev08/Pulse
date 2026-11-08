package com.example.pulse

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pulse.data.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerScreen(
    track: Track,
    isPlaying: Boolean,
    isFavorite: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPause: () -> Unit,
    onFavorite: () -> Unit,
    onClose: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit
) {
    val Background = Color(0xFF101114)
    val Accent = Color(0xFFFF8057)
    val MainText = Color(0xFFF4F3F1)
    val SecondaryText = Color(0xFF9B9CA5)

    // Форматирование времени (мс -> мм:сс)
    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Кнопка "Назад" сверху
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Свернуть",
                        tint = MainText,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Text(
                    text = "СЕЙЧАС ИГРАЕТ",
                    color = SecondaryText,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = {}) {
                    Icon(
                        Icons.Filled.MoreHoriz,
                        contentDescription = "Меню",
                        tint = MainText,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

            // Большая обложка (пока заглушка)
            Surface(
                modifier = Modifier
                    .size(320.dp)
                    .clip(RoundedCornerShape(24.dp)),
                color = Color(0xFF49324C),
                shadowElevation = 20.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = MainText,
                        modifier = Modifier.size(120.dp)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Название и исполнитель
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        color = MainText,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = track.artist,
                        color = SecondaryText,
                        fontSize = 16.sp,
                        maxLines = 1
                    )
                }
                // Сердечко
                IconButton(onClick = onFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Избранное",
                        tint = if (isFavorite) Accent else SecondaryText,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Прогресс-бар
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f,
                    onValueChange = { ratio ->
                        onSeek((ratio * duration).toLong())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = Accent,
                        activeTrackColor = Accent,
                        inactiveTrackColor = Color(0xFF2D2F34)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(currentPosition), color = SecondaryText, fontSize = 12.sp)
                    Text(text = formatTime(duration), color = SecondaryText, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Предыдущий
                IconButton(onClick = onPrevious) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = "Предыдущий",
                        tint = MainText,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Play/Pause (большая)
                Surface(
                    modifier = Modifier.size(72.dp),
                    color = Accent,
                    shape = RoundedCornerShape(36.dp)
                ) {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Background,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Следующий
                IconButton(onClick = onNext) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = "Следующий",
                        tint = MainText,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}