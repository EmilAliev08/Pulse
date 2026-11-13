package com.example.pulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.pulse.data.Track
import com.example.pulse.ui.theme.PulseTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseTheme { MusicApp() }
        }
    }
}

@Composable
fun MusicApp() {
    val authViewModel: AuthViewModel = viewModel()
    val musicViewModel: MusicViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser == null) {
        LoginScreen(onLoginSuccess = {}, viewModel = authViewModel)
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
    val waveTracks by viewModel.waveTracks.collectAsState()
    val isWaveLoading by viewModel.isWaveLoading.collectAsState()

    var showWave by remember { mutableStateOf(false) }
    var showFullPlayer by remember { mutableStateOf(false) }
    var showAuthor by remember { mutableStateOf(false) }
    var selectedArtist by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    LaunchedEffect(selectedTab) {
        showWave = false
        showAuthor = false
    }
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


    Box(modifier = Modifier.fillMaxSize()) {

        // 1. ОСНОВНОЙ КОНТЕНТ (Главная / Избранное)
        when (selectedTab) {
            0 -> MusicScreen(
                tracks = tracks,
                isLoading = isLoading,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                favoriteIds = favoriteIds,
                onSearch = { query -> viewModel.search(query) },
                onTrackClick = { track ->
                    viewModel.playTrack(track, tracks)
                    showFullPlayer = true
                },
                onPlayPauseClick = { viewModel.togglePlay() },
                onFavoriteClick = { track -> viewModel.toggleFavorite(track) },
                onMiniPlayerClick = { showFullPlayer = true },
                onLogout = { authViewModel.logout() },
                onImageClick = { track ->
                    selectedTrackId = track.id
                    imagePickerLauncher.launch("image/*")
                },
                coverUpdateTrigger = coverUpdateTrigger,
                onExploreClick = {
                    showWave = true
                    viewModel.generateWave()
                },
                onArtistClick = { artistName ->
                    selectedArtist = artistName
                    showAuthor = true
                }
            )
            1 -> FavoritesScreen(
                tracks = favoriteTracks,
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                onTrackClick = { track ->
                    viewModel.playTrack(track, favoriteTracks)
                    showFullPlayer = true
                },
                onRemoveFavorite = { track -> viewModel.toggleFavorite(track) },
                onArtistClick = { artistName ->
                    selectedArtist = artistName
                    showAuthor = true
                },
                onImageClick = { track ->
                    selectedTrackId = track.id
                    imagePickerLauncher.launch("image/*")
                },
                coverUpdateTrigger = coverUpdateTrigger
            )
        }


        // 4. ОВЕРЛЕЙ ВОЛНЫ (Теперь работает без ошибки ColumnScope)
        AnimatedVisibility(
            visible = showWave,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            WaveScreen(
                tracks = waveTracks,
                isLoading = isWaveLoading,
                onClose = { showWave = false },
                onTrackClick = { track ->
                    viewModel.playTrack(track, waveTracks)
                    showFullPlayer = true
                },
                onArtistClick = { artistName ->
                    selectedArtist = artistName
                    showAuthor = true
                },
                favoriteIds = favoriteIds,
                onFavoriteClick = { track -> viewModel.toggleFavorite(track) },
                onImageClick = { track ->
                    selectedTrackId = track.id
                    imagePickerLauncher.launch("image/*")
                },
                currentTrackId = currentTrack?.id,
                isPlaying = isPlaying,
                coverUpdateTrigger = coverUpdateTrigger
            )
        }


        // 5. СТРАНИЦА АВТОРА (Теперь работает без ошибки ColumnScope)
        AnimatedVisibility(
            visible = showAuthor,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            AuthorScreen(
                artistName = selectedArtist,
                favoriteIds = favoriteIds,
                currentTrackId = currentTrack?.id ?: 0L,
                isPlaying = isPlaying,
                onClose = { showAuthor = false },
                onTrackClick = { track ->
                    viewModel.playTrack(track)
                    showFullPlayer = true
                },
                onFavoriteClick = { track -> viewModel.toggleFavorite(track) },
                onImageClick = { track ->
                    selectedTrackId = track.id
                    imagePickerLauncher.launch("image/*")
                },
                coverUpdateTrigger = coverUpdateTrigger
            )
        }

        // ===== НИЖНИЙ СТЕК: мини-плеер всегда ровно над навигацией =====
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()   // отступ от системной панели (жесты/кнопки)
        ) {
            // Мини-плеер
            val currentTrackLocal = currentTrack
            if (currentTrackLocal != null && !showFullPlayer) {
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
                    onClose = { viewModel.stopMusic() }
                    // ⚠️ БЕЗ modifier — Column сам ставит его над навигацией
                )
            }

            // Навигация
            if (!showFullPlayer) {
                NavigationBar(
                    containerColor = Color(0xFF1D1F24),
                    contentColor = Color(0xFF9B9CA5),
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0.dp), // ✅ инсеты уже учтены в Column
                    modifier = Modifier.height(64.dp)
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, "Главная", modifier = Modifier.size(20.dp)) },
                        label = { Text("Главная", fontSize = 10.sp) },
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
                        icon = { Icon(Icons.Filled.Favorite, "Избранное", modifier = Modifier.size(20.dp)) },
                        label = { Text("Избранное", fontSize = 10.sp) },
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
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshFavoritePreviews()
    }

    // 6. ПОЛНЫЙ ПЛЕЕР (Снаружи Box, поверх всего)
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
                onClose = { showFullPlayer = false },
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
    coverUpdateTrigger: Int,
    onExploreClick: () -> Unit,
    onArtistClick: (String) -> Unit
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
            .statusBarsPadding()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        item {
            Spacer(Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "pulse.", color = Accent, fontSize = 32.sp, fontWeight = FontWeight.Black)
                IconButton(onClick = onLogout) {
                    Icon(Icons.Filled.Logout, "Выйти", tint = Color(0xFF9B9CA5), modifier = Modifier.size(24.dp))
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
                leadingIcon = { Icon(Icons.Filled.Search, "Поиск", tint = SecondaryText) },
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
                        onClick = { onExploreClick() },
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Background),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Моя волна", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
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

        if (isLoading) {
            item {
                CircularProgressIndicator(color = Accent, modifier = Modifier.fillMaxWidth().padding(top = 40.dp).wrapContentWidth(Alignment.CenterHorizontally))
            }
        } else if (tracks.isEmpty()) {
            item {
                Text(text = "Здесь появятся треки. Нажми на кнопку или введи запрос!", color = SecondaryText, fontSize = 14.sp)
            }
        } else {
            items(tracks, key = { "${it.id}_$coverUpdateTrigger" }) { track ->
                TrackRow(
                    track = track,
                    isPlaying = currentTrack?.id == track.id && isPlaying,
                    isFavorite = favoriteIds.contains(track.id),
                    customCoverPath = CoverManager.getCoverPath(context, track.id),
                    onClick = { onTrackClick(track) },
                    onArtistClick = { onArtistClick(track.artist) },
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
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coverPath = CoverManager.getCoverPath(context, track.id)
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f

    Surface(modifier = modifier.fillMaxWidth().clickable { onClick() }, color = Color(0xFF1D1F24), shadowElevation = 4.dp) {
        Column {
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
                Box(modifier = Modifier.fillMaxWidth(progress).height(3.dp).background(Color(0xFFFF8057)))
            }

            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPreviousClick) {
                    Icon(Icons.Filled.SkipPrevious, "Предыдущий", tint = Color(0xFFF4F3F1), modifier = Modifier.size(24.dp))
                }
                Surface(modifier = Modifier.size(40.dp), color = Color(0xFF49324C), shape = RoundedCornerShape(8.dp)) {
                    if (coverPath != null) {
                        Image(painter = rememberAsyncImagePainter(coverPath), contentDescription = null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                    } else {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Filled.MusicNote, null, tint = Color(0xFFF4F3F1), modifier = Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = track.title, color = Color(0xFFF4F3F1), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    Text(text = track.artist, color = Color(0xFF9B9CA5), fontSize = 11.sp, maxLines = 1)
                }
                IconButton(onClick = onNextClick) {
                    Icon(Icons.Filled.SkipNext, "Следующий", tint = Color(0xFFF4F3F1), modifier = Modifier.size(24.dp))
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, "Скрыть плеер", tint = Color(0xFF9B9CA5), modifier = Modifier.size(20.dp))
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
            tracks = emptyList(), isLoading = false, currentTrack = null, isPlaying = false,
            favoriteIds = emptyList(), onSearch = {}, onTrackClick = {}, onPlayPauseClick = {},
            onFavoriteClick = {}, onMiniPlayerClick = {}, onLogout = {}, onImageClick = {},
            coverUpdateTrigger = 0, onExploreClick = {}, onArtistClick = {}
        )
    }
}