/**
 * EnhancedMusicPlayer.kt - 增强版音乐播放器
 *
 * 使用ExoPlayer支持多种音频格式（MP3, FLAC, ALAC等）
 */
package org.bibichan.union.player.data

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 增强版音乐播放器
 *
 * 使用ExoPlayer提供更好的音频格式支持
 */
class EnhancedMusicPlayer(private val context: Context) {
    
    private val TAG = "EnhancedMusicPlayer"
    
    // ExoPlayer实例
    private val exoPlayer: ExoPlayer = ExoPlayer.Builder(context).build()
    
    // 播放列表
    private var playlist: List<MusicMetadata> = emptyList()
    private var currentIndex: Int = -1
    
    // 播放状态
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentSong = MutableStateFlow<MusicMetadata?>(null)
    val currentSong: StateFlow<MusicMetadata?> = _currentSong.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()
    
    private val _volume = MutableStateFlow(1f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    // 播放模式
    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()
    
    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()
    
    // 播放器监听器
    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }
        
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    _duration.value = exoPlayer.duration
                }
                Player.STATE_ENDED -> {
                    onSongEnded()
                }
            }
        }
    }
    
    init {
        exoPlayer.addListener(playerListener)
    }
    
    /**
     * 设置播放列表
     */
    fun setPlaylist(songs: List<MusicMetadata>) {
        playlist = songs
        if (songs.isNotEmpty()) {
            play(0)
        }
    }
    
    /**
     * 播放指定索引的歌曲
     */
    fun play(index: Int) {
        if (index < 0 || index >= playlist.size) {
            Log.e(TAG, "Invalid index: $index")
            return
        }
        
        currentIndex = index
        val song = playlist[index]
        
        val mediaItem = MediaItem.fromUri(song.uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        
        _currentSong.value = song
        _isPlaying.value = true
        
        Log.d(TAG, "Playing: ${song.title}")
    }
    
    /**
     * 播放指定歌曲
     */
    fun play(song: MusicMetadata) {
        val index = playlist.indexOf(song)
        if (index >= 0) {
            play(index)
        } else {
            // 如果歌曲不在播放列表中，临时播放
            playlist = listOf(song)
            play(0)
        }
    }
    
    /**
     * 暂停播放
     */
    fun pause() {
        exoPlayer.pause()
        _isPlaying.value = false
    }
    
    /**
     * 恢复播放
     */
    fun resume() {
        if (currentIndex >= 0) {
            exoPlayer.play()
            _isPlaying.value = true
        }
    }
    
    /**
     * 停止播放
     */
    fun stop() {
        exoPlayer.stop()
        _isPlaying.value = false
        _currentSong.value = null
        currentIndex = -1
    }
    
    /**
     * 播放下一首
     */
    fun next() {
        if (playlist.isEmpty()) return
        
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                // 单曲循环，不改变索引
            }
            RepeatMode.ALL -> {
                currentIndex = (currentIndex + 1) % playlist.size
            }
            else -> {
                currentIndex = if (currentIndex < playlist.size - 1) 
                    currentIndex + 1 else return
            }
        }
        
        if (_shuffleMode.value) {
            currentIndex = (0 until playlist.size).random()
        }
        
        play(currentIndex)
    }
    
    /**
     * 播放上一首
     */
    fun previous() {
        if (playlist.isEmpty()) return
        
        // 如果播放超过5秒，重新开始当前歌曲
        if (exoPlayer.currentPosition > 5000) {
            seekTo(0)
            return
        }
        
        when (_repeatMode.value) {
            RepeatMode.ALL -> {
                currentIndex = if (currentIndex > 0) 
                    currentIndex - 1 else playlist.size - 1
            }
            else -> {
                currentIndex = if (currentIndex > 0) 
                    currentIndex - 1 else return
            }
        }
        
        if (_shuffleMode.value) {
            currentIndex = (0 until playlist.size).random()
        }
        
        play(currentIndex)
    }
    
    /**
     * 跳转到指定位置
     */
    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }
    
    /**
     * 设置音量
     */
    fun setVolume(volume: Float) {
        exoPlayer.volume = volume
        _volume.value = volume
    }
    
    /**
     * 切换随机播放
     */
    fun toggleShuffle() {
        _shuffleMode.value = !_shuffleMode.value
    }
    
    /**
     * 切换循环模式
     */
    fun toggleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
        
        // 设置ExoPlayer的循环模式
        when (_repeatMode.value) {
            RepeatMode.ONE -> exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
            else -> exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        }
    }
    
    /**
     * 获取当前位置
     */
    fun getCurrentPosition(): Long {
        _currentPosition.value = exoPlayer.currentPosition
        return exoPlayer.currentPosition
    }
    
    /**
     * 获取时长
     */
    fun getDuration(): Long {
        return exoPlayer.duration
    }
    
    /**
     * 是否正在播放
     */
    fun isPlaying(): Boolean {
        return exoPlayer.isPlaying
    }
    
    /**
     * 歌曲结束时的处理
     */
    private fun onSongEnded() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                resume()
            }
            RepeatMode.ALL, RepeatMode.OFF -> {
                next()
            }
        }
    }
    
    /**
     * 清理资源
     */
    fun release() {
        exoPlayer.removeListener(playerListener)
        exoPlayer.release()
    }
    
    /**
     * 播放模式枚举
     */
    enum class RepeatMode {
        OFF,
        ALL,
        ONE
    }
}
