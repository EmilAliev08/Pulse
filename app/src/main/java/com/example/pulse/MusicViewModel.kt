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

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).favoriteDao()
    private val player: ExoPlayer = ExoPlayer.Builder(application).build()
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

    // Список ID избранных треков
    private val _favoriteIds = MutableStateFlow<List<Long>>(emptyList())

    val favoriteIds: StateFlow<List<Long>> = _favoriteIds

    init {
        viewModelScope.launch {
            dao.getAllFavorites().collect { entities ->
                _favoriteIds.value = entities.map { it.id }
            }
        }


        // Слушаем позицию воспроизведения для прогресс-бара
        viewModelScope.launch {
            while (true) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition
                    _duration.value = player.duration
                }
                delay(500) // обновляем 2 раза в секунду
            }
        }

        // Слушаем события плеера
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    _isPlaying.value = false
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
                if (tracks.isEmpty()) {
                    _errorMessage.value = "Ничего не найдено по запросу '$query'"
                }
            }

            result.onFailure { error ->
                _errorMessage.value = "Ошибка: ${error.message}"
                _tracks.value = emptyList()
            }

            _isLoading.value = false
        }
    }

    fun playTrack(track: Track) {
        _currentTrack.value = track

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

    //Добавить убрать из избранного
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

    fun playNext() {}

    fun playPrevious() {}

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}