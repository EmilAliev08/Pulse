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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues

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

    var searchQuery by remember { mutableStateOf("") }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ШАПКА — один большой элемент
            item {
                Spacer(Modifier.height(40.dp))
                Text(text = "pulse.", color = Accent, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(32.dp))
                Text(text = "ТВОЁ МУЗЫКАЛЬНОЕ\nПРОСТРАНСТВО", color = MainText, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(text = "Найди звук под своё настроение.", color = SecondaryText, fontSize = 16.sp)
                Spacer(Modifier.height(40.dp))

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

                Text(text = "Выбери жанр", color = MainText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenreCard("Pop", Icons.Filled.MusicNote, Color(0xFFE91E63)) { onSearch("genre:\"Pop\"") }
                    GenreCard("Rock", Icons.Filled.Headset, Color(0xFFF44336)) { onSearch("genre:\"Rock\"") }
                    GenreCard("Hip-Hop", Icons.Filled.GraphicEq, Color(0xFFFF9800)) { onSearch("genre:\"Hip Hop\"") }
                    GenreCard("Electronic", Icons.Filled.Memory, Color(0xFF00BCD4)) { onSearch("genre:\"Electronic\"") }
                    GenreCard("Jazz", Icons.Filled.Mic, Color(0xFF673AB7)) { onSearch("genre:\"Jazz\"") }
                }
                Spacer(Modifier.height(40.dp))

                Text(text = "Найденные треки", color = MainText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
            }

            // ЗАГРУЗКА
            if (isLoading) {
                item {
                    CircularProgressIndicator(
                        color = Accent,
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp).wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
            // ПУСТО
            else if (tracks.isEmpty()) {
                item {
                    Text(text = "Здесь появятся треки. Нажми на кнопку или введи запрос!", color = SecondaryText, fontSize = 14.sp)
                }
            }
            // ТРЕКИ — каждый трек отдельный элемент списка
            else {
                items(tracks) { track ->
                    TrackRow(
                        track = track,
                        isPlaying = currentTrack?.id == track.id && isPlaying,
                        onClick = { onTrackClick(track) }
                    )
                }
            }
        }
    }
}

@Composable
fun GenreCard(title: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
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