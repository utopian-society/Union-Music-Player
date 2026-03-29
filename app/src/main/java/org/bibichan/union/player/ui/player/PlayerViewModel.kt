package org.bibichan.union.player.ui.player

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.ui.player.models.PlayerState
import org.bibichan.union.player.ui.player.utils.AlbumColorExtractor

class PlayerViewModel(
    private val musicPlayer: MusicPlayer,
    private val context: Context? = null
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state

    private var progressJob: Job? = null
    
    // 顏色提取器 - 使用 Context 創建 ImageLoader
    private val colorExtractor: AlbumColorExtractor? by lazy {
        context?.let { ctx ->
            AlbumColorExtractor(ImageLoader(ctx))
        }
    }

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
                
                // 提取專輯顏色
                val (dominantColor, vibrantColor, darkVibrantColor) = extractAlbumColors(song)
                
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
                    progress = _state.value.progress,
                    dominantColor = dominantColor,
                    vibrantColor = vibrantColor,
                    darkVibrantColor = darkVibrantColor
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

    /**
     * 從專輯封面提取顏色
     *
     * @param song 當前歌曲
     * @return Triple<dominantColor, vibrantColor, darkVibrantColor>
     */
    private suspend fun extractAlbumColors(song: MusicMetadata?): Triple<Color, Color?, Color?> {
        if (song == null || colorExtractor == null) {
            return Triple(Color(0xFF4CAF50), null, null)
        }

        return try {
            val albumArtPath = song.albumArtPath
            val albumColor = colorExtractor!!.extractFromPath(albumArtPath)
            Triple(
                albumColor.dominant,
                albumColor.vibrant,
                albumColor.darkVibrant
            )
        } catch (e: Exception) {
            Triple(Color(0xFF4CAF50), null, null)
        }
    }
}
