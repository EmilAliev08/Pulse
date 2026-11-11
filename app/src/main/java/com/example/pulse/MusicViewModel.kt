package com.example.pulse

import androidx.lifecycle.viewModelScope
import com.example.pulse.data.MusicApi
import com.example.pulse.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.pulse.data.AppDatabase
import com.example.pulse.data.toEntity
import androidx.media3.common.Player
import kotlinx.coroutines.delay
import com.example.pulse.data.toTrack

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).favoriteDao()
    private val player: ExoPlayer = ExoPlayer.Builder(application).build()

    // Очередь треков
    private val _playlist = MutableStateFlow<List<Track>>(emptyList())
    val playlist: StateFlow<List<Track>> = _playlist

    private val _playlistIndex = MutableStateFlow(0)
    val playlistIndex: StateFlow<Int> = _playlistIndex

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isPlayerReady = MutableStateFlow(false)
    val isPlayerReady: StateFlow<Boolean> = _isPlayerReady

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    private val _favoriteIds = MutableStateFlow<List<Long>>(emptyList())
    val favoriteIds: StateFlow<List<Long>> = _favoriteIds

    private val _favoriteTracks = MutableStateFlow<List<Track>>(emptyList())
    val favoriteTracks: StateFlow<List<Track>> = _favoriteTracks

    init {
        viewModelScope.launch {
            dao.getAllFavorites().collect { entities ->
                _favoriteIds.value = entities.map { it.id }
                _favoriteTracks.value = entities.map { it.toTrack() }
            }
        }

        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition
                    _duration.value = player.duration
                }
                delay(500)
            }
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    playNext() // Автопереход на следующий трек
                }
            }
        })
    }

    fun search(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = MusicApi.search(query)
            result.onSuccess { tracks ->
                _tracks.value = tracks
                if (tracks.isEmpty()) _errorMessage.value = "Ничего не найдено по запросу '$query'"
            }
            result.onFailure { error ->
                _errorMessage.value = "Ошибка: ${error.message}"
                _tracks.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    fun playTrack(track: Track, playlist: List<Track> = emptyList()) {
        _currentTrack.value = track

        // Формируем очередь
        if (playlist.isNotEmpty()) {
            _playlist.value = playlist
            _playlistIndex.value = playlist.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        } else if (_playlist.value.none { it.id == track.id }) {
            _playlist.value = listOf(track)
            _playlistIndex.value = 0
        } else {
            _playlistIndex.value = _playlist.value.indexOfFirst { it.id == track.id }
        }

        val mediaItem = MediaItem.fromUri(track.previewUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _isPlaying.value = true
        _isPlayerReady.value = true
    }

    fun togglePlay() {
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else {
            player.play()
            _isPlaying.value = true
        }
    }

    fun toggleFavorite(track: Track) {
        viewModelScope.launch {
            if (_favoriteIds.value.contains(track.id)) {
                dao.remove(track.id)
            } else {
                dao.add(track.toEntity())
            }
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
        _currentPosition.value = position
    }

    fun stopMusic() {
        player.stop()
        player.clearMediaItems()
        _isPlaying.value = false
        _currentTrack.value = null
        _currentPosition.value = 0
        _duration.value = 0
    }

    fun playNext() {
        val current = _playlist.value
        val index = _playlistIndex.value
        if (current.isEmpty()) return
        val nextIndex = (index + 1) % current.size
        _playlistIndex.value = nextIndex
        playTrack(current[nextIndex])
    }

    fun playPrevious() {
        val current = _playlist.value
        val index = _playlistIndex.value
        if (current.isEmpty()) return

        // Если трек играет >3 сек, перемотать в начало, иначе предыдущий
        if (player.currentPosition > 3000) {
            player.seekTo(0)
            _currentPosition.value = 0
            return
        }

        val prevIndex = if (index - 1 < 0) current.size - 1 else index - 1
        _playlistIndex.value = prevIndex
        playTrack(current[prevIndex])
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}