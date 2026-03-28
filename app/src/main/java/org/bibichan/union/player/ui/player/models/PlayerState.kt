package org.bibichan.union.player.ui.player.models

import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicMetadata

data class PlayerState(
    val isExpanded: Boolean = false,
    val currentPage: Int = 0,
    val isPlaying: Boolean = false,
    val progress: Float = 0f,
    val currentTimeMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentSong: MusicMetadata? = null,
    val audioFormat: String = "MP3",
    val sampleRateHz: Int? = null,
    val bitDepth: Int? = null,
    val lyricsText: String? = null,
    val shuffleEnabled: Boolean = false,
    val repeatMode: MusicPlayer.RepeatMode = MusicPlayer.RepeatMode.OFF
)
