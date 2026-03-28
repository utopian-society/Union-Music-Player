package org.bibichan.union.player.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.ui.player.models.PlayerState

class PlayerViewModel(
    private val musicPlayer: MusicPlayer
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state

    private var progressJob: Job? = null

    init {
        observePlayerState()
        startProgressUpdates()
    }

    fun playPause() {
        if (musicPlayer.isPlaying()) {
            musicPlayer.pause()
        } else {
            musicPlayer.resume()
        }
    }

    fun next() {
        musicPlayer.next()
    }

    fun previous() {
        musicPlayer.previous()
    }

    fun toggleShuffle() {
        musicPlayer.toggleShuffle()
    }

    fun cycleRepeatMode() {
        musicPlayer.cycleRepeatMode()
    }

    fun seekTo(positionMs: Long) {
        musicPlayer.seekTo(positionMs)
    }

    fun onExpandedChange(expanded: Boolean) {
        // UI-owned state, handled by PlayerScreen or caller; no-op for now.
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            combine(
                musicPlayer.currentSongFlow,
                musicPlayer.isPlayingFlow,
                musicPlayer.lyricsFlow,
                musicPlayer.shuffleEnabledFlow,
                musicPlayer.repeatModeFlow
            ) { song, isPlaying, lyrics, shuffleEnabled, repeatMode ->
                val (formatLabel, sampleRateHz, bitDepth) = extractAudioInfo(song)
                PlayerState(
                    isPlaying = isPlaying,
                    currentSong = song,
                    audioFormat = formatLabel,
                    sampleRateHz = sampleRateHz,
                    bitDepth = bitDepth,
                    lyricsText = lyrics,
                    shuffleEnabled = shuffleEnabled,
                    repeatMode = repeatMode,
                    currentTimeMs = _state.value.currentTimeMs,
                    durationMs = _state.value.durationMs,
                    progress = _state.value.progress
                )
            }.collect { updated ->
                _state.value = updated
            }
        }
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                val duration = musicPlayer.getDuration().coerceAtLeast(0L)
                val position = musicPlayer.getCurrentPosition().coerceAtLeast(0L)
                val progress = if (duration > 0L) {
                    (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }

                val current = _state.value
                _state.value = current.copy(
                    currentTimeMs = position,
                    durationMs = duration,
                    progress = progress
                )
                delay(500)
            }
        }
    }

    private fun extractAudioInfo(song: MusicMetadata?): Triple<String, Int?, Int?> {
        if (song == null) {
            return Triple("MP3", null, null)
        }
        return Triple(song.format.displayName, song.sampleRateHz, song.bitDepth)
    }
}
