/**
 * MusicPlayer.kt - 音乐播放器封装类（使用ExoPlayer）
 *
 * 这个类封装了Google的ExoPlayer，提供更强大的音频播放能力。
 * ExoPlayer支持多种音频格式：MP3, FLAC, ALAC, WAV, AAC, OGG等。
 *
 * 学习要点：
 * 1. ExoPlayer的生命周期管理
 * 2. 音频焦点管理
 * 3. 多种音频格式的支持
 * 4. 状态管理和回调
 *
 * 2026-03-24: 增加 StateFlow 以便 Compose 响应式显示浮动播放器
 */
package org.bibichan.union.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.bibichan.union.player.data.AudioFormat
import org.bibichan.union.player.data.MusicMetadata

/**
 * MusicPlayer类 - 音乐播放器封装
 */
class MusicPlayer(private val context: Context) {

    private var exoPlayer: ExoPlayer? = null

    private var currentSongIndex: Int = -1

    private var songs: List<MusicMetadata> = emptyList()

    private var playbackListener: PlaybackListener? = null

    private val TAG = "MusicPlayer"

    private var isPlayingState: Boolean = false

    private val _currentSongFlow = MutableStateFlow<MusicMetadata?>(null)
    val currentSongFlow: StateFlow<MusicMetadata?> = _currentSongFlow.asStateFlow()

    private val _isPlayingFlow = MutableStateFlow(false)
    val isPlayingFlow: StateFlow<Boolean> = _isPlayingFlow.asStateFlow()

    private val _lastErrorFlow = MutableStateFlow<String?>(null)
    val lastErrorFlow: StateFlow<String?> = _lastErrorFlow.asStateFlow()

    private val _playbackStateFlow = MutableStateFlow(Player.STATE_IDLE)
    val playbackStateFlow: StateFlow<Int> = _playbackStateFlow.asStateFlow()

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        try {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build()

                setAudioAttributes(audioAttributes, true)

                setHandleAudioBecomingNoisy(true)

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _playbackStateFlow.value = playbackState
                        when (playbackState) {
                            Player.STATE_READY -> {
                                Log.d(TAG, "Player ready")
                                playbackListener?.onReady()
                            }
                            Player.STATE_ENDED -> {
                                Log.d(TAG, "Song ended")
                                playbackListener?.onSongEnded()
                                next()
                            }
                            Player.STATE_BUFFERING -> {
                                Log.d(TAG, "Buffering...")
                            }
                            Player.STATE_IDLE -> {
                                Log.d(TAG, "Player idle")
                            }
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        isPlayingState = isPlaying
                        _isPlayingFlow.value = isPlaying
                        playbackListener?.onPlayingChanged(isPlaying)
                        Log.d(TAG, "Is playing: $isPlaying")
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        val message = "Playback error: ${error.errorCodeName}"
                        Log.e(TAG, message, error)
                        _lastErrorFlow.value = message
                        playbackListener?.onError(message)
                    }
                })
            }
            Log.d(TAG, "ExoPlayer initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ExoPlayer", e)
            _lastErrorFlow.value = "Player init error: ${e.message}"
        }
    }

    fun setPlaybackListener(listener: PlaybackListener) {
        this.playbackListener = listener
    }

    fun setSongs(songs: List<MusicMetadata>) {
        this.songs = songs
        Log.d(TAG, "Songs set: ${songs.size} songs")

        if (songs.isEmpty()) {
            currentSongIndex = -1
            _currentSongFlow.value = null
            _isPlayingFlow.value = false
        } else if (currentSongIndex !in songs.indices) {
            currentSongIndex = -1
            _currentSongFlow.value = null
        }
    }

    fun play(songIndex: Int) {
        if (songIndex < 0 || songIndex >= songs.size) {
            Log.e(TAG, "Invalid song index: $songIndex")
            return
        }

        try {
            currentSongIndex = songIndex

            val song = songs[currentSongIndex]
            Log.d(TAG, "Playing: ${song.title} by ${song.artist}")
            Log.d(TAG, "Format: ${song.format}, Path: ${song.filePath}")

            _currentSongFlow.value = song
            _lastErrorFlow.value = null

            val mediaItem = createMediaItem(song)

            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }

            _isPlayingFlow.value = true

            playbackListener?.onSongChanged(song, songIndex)

        } catch (e: Exception) {
            Log.e(TAG, "Error playing song", e)
            val message = "Error playing song: ${e.message}"
            _lastErrorFlow.value = message
            playbackListener?.onError(message)
        }
    }

    private fun createMediaItem(song: MusicMetadata): MediaItem {
        val builder = MediaItem.Builder()

        val uri = when {
            song.uri != Uri.EMPTY -> song.uri
            song.filePath.isNotEmpty() -> Uri.fromFile(java.io.File(song.filePath))
            else -> {
                Log.e(TAG, "No valid URI or file path for song: ${song.title}")
                Uri.EMPTY
            }
        }

        builder.setUri(uri)
            .setMediaId(song.id.toString())
            .setMediaMetadata(
                androidx.media3.common.MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setRecordingYear(song.year ?: 0)
                    .setTrackNumber(song.trackNumber ?: 0)
                    .build()
            )

        val mimeType = when (song.format) {
            AudioFormat.MP3 -> "audio/mpeg"
            AudioFormat.FLAC -> "audio/flac"
            AudioFormat.ALAC -> "audio/mp4"
            AudioFormat.WAV -> "audio/wav"
            AudioFormat.AAC -> "audio/mp4a-latm"
            AudioFormat.OGG -> "audio/ogg"
            AudioFormat.M4A -> "audio/mp4"
            else -> null
        }

        mimeType?.let { builder.setMimeType(it) }

        return builder.build()
    }

    fun pause() {
        try {
            exoPlayer?.pause()
            _isPlayingFlow.value = false
            Log.d(TAG, "Paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing", e)
        }
    }

    fun resume() {
        try {
            if (exoPlayer != null && !isPlaying()) {
                exoPlayer?.play()
                _isPlayingFlow.value = true
                Log.d(TAG, "Resumed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming", e)
        }
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }

    fun next() {
        if (songs.isEmpty()) {
            Log.e(TAG, "No songs in playlist")
            return
        }

        val nextSongIndex = (currentSongIndex + 1) % songs.size
        play(nextSongIndex)
    }

    fun previous() {
        if (songs.isEmpty()) {
            Log.e(TAG, "No songs in playlist")
            return
        }

        val previousSongIndex = if (currentSongIndex - 1 < 0) {
            songs.size - 1
        } else {
            currentSongIndex - 1
        }
        play(previousSongIndex)
    }

    fun seekTo(position: Long) {
        try {
            exoPlayer?.seekTo(position)
            Log.d(TAG, "Seeked to: $position ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking", e)
        }
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }

    fun getCurrentSong(): MusicMetadata? {
        return if (currentSongIndex >= 0 && currentSongIndex < songs.size) {
            songs[currentSongIndex]
        } else {
            null
        }
    }

    fun getCurrentSongIndex(): Int {
        return currentSongIndex
    }

    fun setPlaybackSpeed(speed: Float) {
        try {
            exoPlayer?.setPlaybackSpeed(speed)
            Log.d(TAG, "Playback speed set to: $speed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting playback speed", e)
        }
    }

    fun setVolume(volume: Float) {
        try {
            exoPlayer?.volume = volume.coerceIn(0f, 1f)
            Log.d(TAG, "Volume set to: $volume")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
        }
    }

    fun getVolume(): Float {
        return exoPlayer?.volume ?: 1f
    }

    fun release() {
        try {
            exoPlayer?.release()
            exoPlayer = null
            currentSongIndex = -1
            songs = emptyList()
            playbackListener = null
            _currentSongFlow.value = null
            _isPlayingFlow.value = false
            _lastErrorFlow.value = null
            _playbackStateFlow.value = Player.STATE_IDLE
            Log.d(TAG, "ExoPlayer released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing ExoPlayer", e)
        }
    }

    interface PlaybackListener {
        fun onSongChanged(song: MusicMetadata, index: Int) {}

        fun onPlayingChanged(isPlaying: Boolean) {}

        fun onReady() {}

        fun onSongEnded() {}

        fun onError(message: String) {}
    }
}
