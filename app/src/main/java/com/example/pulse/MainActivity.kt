package com.example.pulse

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.size
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.net.Uri
import java.io.File
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip

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
    val authViewModel: AuthViewModel = viewModel()
    val musicViewModel: MusicViewModel = viewModel()

    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {
        LoginScreen(
            onLoginSuccess = { },
            viewModel = authViewModel
        )
    } else {
        MainAppContent(viewModel = musicViewModel, authViewModel = authViewModel)
    }
}

@Composable
fun MainAppContent(viewModel: MusicViewModel, authViewModel: AuthViewModel) {
    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val favoriteTracks by viewModel.favoriteTracks.collectAsState()


    var showFullPlayer by remember { mutableStateOf(false) }
    var showMiniPlayer by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedTrackId by remember { mutableLongStateOf(0L) }
    var coverUpdateTrigger by remember { mutableIntStateOf(0) }

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (selectedTrackId > 0) {
                CoverManager.saveCover(context, selectedTrackId, it)
                coverUpdateTrigger++
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Контент в зависимости от вкладки
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> MusicScreen(
                    tracks = tracks,
                    isLoading = isLoading,
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    favoriteIds = favoriteIds,
                    onSearch = { query -> viewModel.search(query) },
                    onTrackClick = { track ->
                        viewModel.playTrack(track, tracks) // <-- Передаем список поиска
                        showFullPlayer = true
                        showMiniPlayer = true
                    },
                    onPlayPauseClick = { viewModel.togglePlay() },
                    onFavoriteClick = { track -> viewModel.toggleFavorite(track) },
                    onMiniPlayerClick = { showFullPlayer = true },
                    onLogout = { authViewModel.logout() },
                    onImageClick = { track ->
                        selectedTrackId = track.id
                        imagePickerLauncher.launch("image/*")
                    },
                    coverUpdateTrigger = coverUpdateTrigger
                )
                1 -> FavoritesScreen(
                    tracks = favoriteTracks,
                    currentTrack = currentTrack,
                    isPlaying = isPlaying,
                    onTrackClick = { track ->
                        viewModel.playTrack(track, favoriteTracks) // <-- Передаем избранное
                        showFullPlayer = true
                        showMiniPlayer = true
                    },
                    onRemoveFavorite = { track -> viewModel.toggleFavorite(track) }
                )
            }
        }

        // Мини-плеер
        val currentTrackLocal = currentTrack
        if (currentTrackLocal != null && showMiniPlayer) {
            MiniPlayer(
                track = currentTrackLocal,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                onPlayPauseClick = { viewModel.togglePlay() },
                onSeek = { position -> viewModel.seekTo(position) },
                onNextClick = { viewModel.playNext() },
                onPreviousClick = { viewModel.playPrevious() },
                onClick = { showFullPlayer = true },
                onClose = {
                    showMiniPlayer = false
                    viewModel.stopMusic()
                }
            )
        }

        // Компактная нижняя навигация
        NavigationBar(
            containerColor = Color(0xFF1D1F24),
            contentColor = Color(0xFF9B9CA5),
            tonalElevation = 0.dp,
            modifier = Modifier.height(84.dp)
        ) {
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Главная",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text("Главная", fontSize = 11.sp) },
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF8057),
                    selectedTextColor = Color(0xFFFF8057),
                    unselectedIconColor = Color(0xFF9B9CA5),
                    unselectedTextColor = Color(0xFF9B9CA5),
                    indicatorColor = Color.Transparent
                )
            )
            NavigationBarItem(
                icon = {
                    Icon(
                        Icons.Filled.Favorite,
                        contentDescription = "Избранное",
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text("Избранное", fontSize = 11.sp) },
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFFFF8057),
                    selectedTextColor = Color(0xFFFF8057),
                    unselectedIconColor = Color(0xFF9B9CA5),
                    unselectedTextColor = Color(0xFF9B9CA5),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }


    AnimatedVisibility(
        visible = showFullPlayer && currentTrack != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        currentTrack?.let { track ->
            FullPlayerScreen(
                track = track,
                isPlaying = isPlaying,
                isFavorite = favoriteIds.contains(track.id),
                currentPosition = currentPosition,
                duration = duration,
                onPlayPause = { viewModel.togglePlay() },
                onFavorite = { viewModel.toggleFavorite(track) },
                onClose = {
                    showFullPlayer = false
                    showMiniPlayer = true
                },
                onSeek = { position -> viewModel.seekTo(position) },
                onNext = { viewModel.playNext() },
                onPrevious = { viewModel.playPrevious() }
            )
        }
    }
}

@Composable
fun MusicScreen(
    tracks: List<Track>,
    isLoading: Boolean,
    currentTrack: Track?,
    isPlaying: Boolean,
    favoriteIds: List<Long>,
    onSearch: (String) -> Unit,
    onTrackClick: (Track) -> Unit,
    onPlayPauseClick: () -> Unit,
    onFavoriteClick: (Track) -> Unit,
    onMiniPlayerClick: () -> Unit,
    onLogout: () -> Unit,
    onImageClick: (Track) -> Unit,
    coverUpdateTrigger: Int
) {
    val Background = Color(0xFF101114)
    val Accent = Color(0xFFFF8057)
    val MainText = Color(0xFFF4F3F1)
    val SecondaryText = Color(0xFF9B9CA5)

    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
            // шапка один большой элемент
            item {
                Spacer(Modifier.height(40.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "pulse.", color = Accent, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Filled.Logout,
                            contentDescription = "Выйти",
                            tint = Color(0xFF9B9CA5),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
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
                items(tracks, key = { "${it.id}_$coverUpdateTrigger" }) { track ->
                    TrackRow(
                        track = track,
                        isPlaying = currentTrack?.id == track.id && isPlaying,
                        isFavorite = favoriteIds.contains(track.id),
                        customCoverPath = CoverManager.getCoverPath(context, track.id),
                        onClick = { onTrackClick(track) },
                        onFavoriteClick = { onFavoriteClick(track) },
                        onImageClick = { onImageClick(track) }
                    )
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
fun TrackRow(
    track: Track,
    isPlaying: Boolean,
    isFavorite: Boolean,
    customCoverPath: String? = null,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onImageClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Иконка ноты
        Surface(
            modifier = Modifier.size(56.dp),
            color = Color(0xFF49324C),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (customCoverPath != null) {
                // Показываем загруженную картинку
                Image(
                    painter = rememberAsyncImagePainter(customCoverPath),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
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

        IconButton(onClick = onImageClick) {
            Icon(
                Icons.Filled.Landscape,
                contentDescription = "Предложить картинку",
                tint = Color(0xFF9B9CA5),
                modifier = Modifier.size(24.dp)
            )
        }
        // СЕРДЕЧКО
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

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onNextClick: () -> Unit,   // <-- НОВЫЙ ПАРАМЕТР
    onPreviousClick: () -> Unit, // <-- НОВЫЙ ПАРАМЕТР
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coverPath = CoverManager.getCoverPath(context, track.id)
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f

    Surface(
        modifier = modifier.fillMaxWidth().clickable { onClick() },
        color = Color(0xFF1D1F24),
        shadowElevation = 4.dp
    ) {
        Column {
            // Полоска прогресса
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(Color(0xFF2D2F34))
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            if (duration > 0) {
                                val clickRatio = (offset.x / size.width).coerceIn(0f, 1f)
                                onSeek((clickRatio * duration).toLong())
                            }
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(3.dp)
                        .background(Color(0xFFFF8057))
                )
            }

            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous
                IconButton(onClick = onPreviousClick) {
                    Icon(Icons.Filled.SkipPrevious, "Предыдущий",
                        tint = Color(0xFFF4F3F1), modifier = Modifier.size(24.dp))
                }

                // Обложка
                Surface(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFF49324C),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (coverPath != null) {
                        Image(
                            painter = rememberAsyncImagePainter(coverPath),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.MusicNote, null,
                                tint = Color(0xFFF4F3F1), modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))

                // Текст
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = track.title, color = Color(0xFFF4F3F1), fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(text = track.artist, color = Color(0xFF9B9CA5), fontSize = 11.sp, maxLines = 1)
                }

                // Next
                IconButton(onClick = onNextClick) {
                    Icon(Icons.Filled.SkipNext, "Следующий",
                        tint = Color(0xFFF4F3F1), modifier = Modifier.size(24.dp))
                }

                // Close
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "Скрыть плеер",
                        tint = Color(0xFF9B9CA5), modifier = Modifier.size(20.dp))
                }
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
            favoriteIds = emptyList(),
            onSearch = {},
            onTrackClick = {},
            onPlayPauseClick = {},
            onFavoriteClick = {},
            onMiniPlayerClick = {},
            onLogout = {},
            onImageClick = {},
            coverUpdateTrigger = 0
        )
    }
}