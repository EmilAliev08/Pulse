package com.example.pulse

import androidx.lifecycle.ViewModel
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

class MusicViewModel(application: Application) : AndroidViewModel(application) {
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

        // ссылка в объект
        val mediaItem = MediaItem.fromUri(track.previewUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
        _isPlaying.value = true
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

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}