package com.example.pulse

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pulse.data.Track
import com.example.pulse.ui.theme.PulseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseTheme {
                MusicApp()
            }
        }
    }
}

@Composable
fun MusicApp() {
    val viewModel: MusicViewModel = viewModel()
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()

    val context = LocalContext.current
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    MusicScreen(
        tracks = tracks,
        isLoading = isLoading,
        currentTrack = currentTrack,
        isPlaying = isPlaying,
        onSearch = { query -> viewModel.search(query) },
        onTrackClick = { track -> viewModel.playTrack(track) },
        onPlayPauseClick = { viewModel.togglePlay() }
    )
}

@Composable
fun MusicScreen(
    tracks: List<Track>,
    isLoading: Boolean,
    currentTrack: Track?,
    isPlaying: Boolean,
    onSearch: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit
) {
    val Background = Color(0xFF101114)
    val Accent = Color(0xFFFF8057)
    val MainText = Color(0xFFF4F3F1)
    val SecondaryText = Color(0xFF9B9CA5)

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (currentTrack != null) {
                MiniPlayer(
                    track = currentTrack,
                    isPlaying = isPlaying,
                    onPlayPauseClick = onPlayPauseClick
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(40.dp))
            Text(text = "pulse.", color = Accent, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.height(32.dp))
            Text(text = "ТВОЁ МУЗЫКАЛЬНОЕ\nПРОСТРАНСТВО", color = MainText, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(text = "Найди звук под своё настроение.", color = SecondaryText, fontSize = 16.sp)
            Spacer(Modifier.height(40.dp))

            var searchQuery by remember { mutableStateOf("") }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Что слушаем сегодня?") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Поиск", tint = SecondaryText) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF2D2F34),
                    focusedTextColor = MainText, unfocusedTextColor = MainText,
                    focusedPlaceholderColor = SecondaryText, unfocusedPlaceholderColor = SecondaryText,
                    focusedLeadingIconColor = SecondaryText, unfocusedLeadingIconColor = SecondaryText
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { if (searchQuery.isNotBlank()) onSearch(searchQuery) })
            )
            Spacer(Modifier.height(32.dp))

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = Color(0xFF29221F)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "PULSE SELECTION", color = Accent, fontSize = 12.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text(text = "Включи\nсвой момент.", color = MainText, fontSize = 32.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(text = "Музыка для твоего ритма жизни.", color = SecondaryText, fontSize = 15.sp)
                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = { onSearch("top hits 2026") },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Background),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text(text = "Исследовать музыку", fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                }
            }
            Spacer(Modifier.height(40.dp))

            Text(text = "Выбери настроение", color = MainText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MoodCard("Спокойствие", Icons.Filled.Cloud, Color(0xFF293C46)) { onSearch("chill relax") }
                MoodCard("Энергия", Icons.Filled.Bolt, Color(0xFF453027)) { onSearch("rock energy") }
                MoodCard("Фокус", Icons.Filled.CenterFocusStrong, Color(0xFF35304A)) { onSearch("lo-fi study") }
                MoodCard("Ночь", Icons.Filled.NightsStay, Color(0xFF263348)) { onSearch("night jazz") }
            }
            Spacer(Modifier.height(40.dp))

            Text(text = "Найденные треки", color = MainText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Accent, modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (tracks.isEmpty()) {
                Text(text = "Здесь появятся треки. Нажми на кнопку или введи запрос!", color = SecondaryText, fontSize = 14.sp)
            } else {
                tracks.forEach { track ->
                    TrackRow(
                        track = track,
                        isPlaying = currentTrack?.id == track.id && isPlaying,
                        onClick = { onTrackClick(track) }
                    )
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
fun MoodCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Surface(modifier = Modifier.width(150.dp).clickable { onClick() }, color = color, shape = RoundedCornerShape(18.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFFF4F3F1), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(20.dp))
            Text(text = title, color = Color(0xFFF4F3F1), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun TrackRow(track: Track, isPlaying: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка ноты вместо обложки
        Surface(
            modifier = Modifier.size(56.dp),
            color = Color(0xFF49324C),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFFF4F3F1),
                    modifier = Modifier.size(28.dp)
                )
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
            Text(text = track.artist, color = Color(0xFF9B9CA5), fontSize = 14.sp, maxLines = 1)
        }
        if (isPlaying) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Играет",
                tint = Color(0xFFFF8057),
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Меню",
                tint = Color(0xFF9B9CA5),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun MiniPlayer(track: Track, isPlaying: Boolean, onPlayPauseClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color(0xFF1D1F24),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                color = Color(0xFF49324C),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFFF4F3F1),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = track.title, color = Color(0xFFF4F3F1), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(text = track.artist, color = Color(0xFF9B9CA5), fontSize = 12.sp, maxLines = 1)
            }
            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Управление",
                    tint = Color(0xFFFF8057),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MusicScreenPreview() {
    PulseTheme {
        MusicScreen(
            tracks = emptyList(),
            isLoading = false,
            currentTrack = null,
            isPlaying = false,
            onSearch = {},
            onTrackClick = {},
            onPlayPauseClick = {}
        )
    }
}