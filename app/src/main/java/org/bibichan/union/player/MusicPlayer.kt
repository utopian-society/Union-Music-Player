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
 */
package org.bibichan.union.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import org.bibichan.union.player.data.AudioFormat
import org.bibichan.union.player.data.MusicMetadata

/**
 * MusicPlayer类 - 音乐播放器封装
 *
 * 使用ExoPlayer替代MediaPlayer，支持更多音频格式：
 * - MP3 (MPEG-1 Audio Layer III)
 * - FLAC (Free Lossless Audio Codec)
 * - ALAC (Apple Lossless Audio Codec)
 * - WAV (Waveform Audio File Format)
 * - AAC (Advanced Audio Coding)
 * - OGG (Ogg Vorbis)
 *
 * @param context 上下文对象，用于创建ExoPlayer
 */
class MusicPlayer(private val context: Context) {
    
    // ==================== 私有属性 ====================
    
    /**
     * ExoPlayer实例 - Google开发的高级媒体播放器
     * 
     * 优势：
     * 1. 支持更多音频格式（FLAC, ALAC等）
     * 2. 更好的性能和稳定性
     * 3. 支持无缝循环播放
     * 4. 支持音频焦点管理
     * 5. 支持后台播放
     */
    private var exoPlayer: ExoPlayer? = null
    
    /**
     * 当前播放歌曲的索引
     * 初始值为-1，表示没有歌曲在播放
     */
    private var currentSongIndex: Int = -1
    
    /**
     * 歌曲列表
     * 使用List<MusicMetadata>类型，包含完整的音乐元数据
     */
    private var songs: List<MusicMetadata> = emptyList()
    
    /**
     * 播放状态回调接口
     */
    private var playbackListener: PlaybackListener? = null
    
    /**
     * 日志标签，用于调试
     */
    private val TAG = "MusicPlayer"
    
    /**
     * 是否正在播放
     */
    private var isPlayingState: Boolean = false
    
    // ==================== 初始化 ====================
    
    init {
        initializePlayer()
    }
    
    /**
     * 初始化ExoPlayer
     * 
     * 配置说明：
     * - setAudioAttributes: 配置音频属性（音频焦点）
     * - setHandleAudioBecomingNoisy: 处理耳机断开事件
     * - addListener: 添加播放状态监听器
     */
    private fun initializePlayer() {
        try {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                // 配置音频属性，处理音频焦点
                // 当其他应用播放音频时，自动暂停当前播放
                val audioAttributes = androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build()
                
                setAudioAttributes(audioAttributes, true)
                
                // 处理耳机断开事件
                setHandleAudioBecomingNoisy(true)
                
                // 添加播放状态监听器
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_READY -> {
                                Log.d(TAG, "Player ready")
                                playbackListener?.onReady()
                            }
                            Player.STATE_ENDED -> {
                                Log.d(TAG, "Song ended")
                                playbackListener?.onSongEnded()
                                // 自动播放下一首
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
                        playbackListener?.onPlayingChanged(isPlaying)
                        Log.d(TAG, "Is playing: $isPlaying")
                    }
                })
            }
            Log.d(TAG, "ExoPlayer initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ExoPlayer", e)
        }
    }
    
    // ==================== 公共方法 ====================
    
    /**
     * 设置播放状态回调
     * 
     * @param listener 播放状态监听器
     */
    fun setPlaybackListener(listener: PlaybackListener) {
        this.playbackListener = listener
    }
    
    /**
     * 设置歌曲列表
     * 
     * @param songs 歌曲列表（包含完整元数据）
     */
    fun setSongs(songs: List<MusicMetadata>) {
        this.songs = songs
        Log.d(TAG, "Songs set: ${songs.size} songs")
    }
    
    /**
     * 播放指定索引的歌曲
     * 
     * 播放流程：
     * 1. 检查索引是否有效
     * 2. 获取歌曲文件路径或URI
     * 3. 创建MediaItem
     * 4. 设置到ExoPlayer并开始播放
     * 
     * 支持的音频格式：
     * - MP3: 最常见的有损压缩格式
     * - FLAC: 无损压缩格式，音质优秀
     * - ALAC: Apple无损压缩格式
     * - WAV: 无压缩格式，音质最佳但文件大
     * 
     * @param songIndex 歌曲在列表中的索引（从0开始）
     */
    fun play(songIndex: Int) {
        // 检查索引是否有效
        if (songIndex < 0 || songIndex >= songs.size) {
            Log.e(TAG, "Invalid song index: $songIndex")
            return
        }
        
        try {
            // 更新当前歌曲索引
            currentSongIndex = songIndex
            
            // 获取要播放的歌曲
            val song = songs[currentSongIndex]
            Log.d(TAG, "Playing: ${song.title} by ${song.artist}")
            Log.d(TAG, "Format: ${song.format}, Path: ${song.filePath}")
            
            // 创建MediaItem（支持本地文件和URI）
            val mediaItem = createMediaItem(song)
            
            // 设置媒体项并开始播放
            exoPlayer?.apply {
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
            }
            
            // 通知回调
            playbackListener?.onSongChanged(song, songIndex)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error playing song", e)
            playbackListener?.onError("Error playing song: ${e.message}")
        }
    }
    
    /**
     * 创建MediaItem
     * 
     * 根据歌曲的URI或文件路径创建MediaItem。
     * 支持多种音频格式的自动识别：
     * - MP3: audio/mpeg
     * - FLAC: audio/flac
     * - ALAC: audio/mp4 (在MP4容器中)
     * - WAV: audio/wav
     * - AAC: audio/mp4a-latm
     * - OGG: audio/ogg
     * 
     * @param song 歌曲元数据
     * @return MediaItem实例
     */
    private fun createMediaItem(song: MusicMetadata): MediaItem {
        val builder = MediaItem.Builder()
        
        // 设置URI（优先使用URI，其次使用文件路径）
        val uri = when {
            song.uri != Uri.EMPTY -> song.uri
            song.filePath.isNotEmpty() -> Uri.fromFile(java.io.File(song.filePath))
            else -> {
                Log.e(TAG, "No valid URI or file path for song: ${song.title}")
                Uri.EMPTY
            }
        }
        
        // 设置媒体元数据
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
        
        // 根据音频格式设置MIME类型（可选，ExoPlayer可以自动检测）
        val mimeType = when (song.format) {
            AudioFormat.MP3 -> "audio/mpeg"
            AudioFormat.FLAC -> "audio/flac"
            AudioFormat.ALAC -> "audio/mp4" // ALAC通常在MP4容器中
            AudioFormat.WAV -> "audio/wav"
            AudioFormat.AAC -> "audio/mp4a-latm"
            AudioFormat.OGG -> "audio/ogg"
            AudioFormat.M4A -> "audio/mp4"
            else -> null // 让ExoPlayer自动检测
        }
        
        mimeType?.let { builder.setMimeType(it) }
        
        return builder.build()
    }
    
    /**
     * 暂停播放
     */
    fun pause() {
        try {
            exoPlayer?.pause()
            Log.d(TAG, "Paused")
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing", e)
        }
    }
    
    /**
     * 恢复播放
     */
    fun resume() {
        try {
            if (exoPlayer != null && !isPlaying()) {
                exoPlayer?.play()
                Log.d(TAG, "Resumed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming", e)
        }
    }
    
    /**
     * 检查是否正在播放
     * 
     * @return true表示正在播放，false表示未播放或播放器未初始化
     */
    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: false
    }
    
    /**
     * 播放下一首歌曲
     * 
     * 如果当前是最后一首，循环到第一首（循环播放）
     */
    fun next() {
        if (songs.isEmpty()) {
            Log.e(TAG, "No songs in playlist")
            return
        }
        
        val nextSongIndex = (currentSongIndex + 1) % songs.size
        play(nextSongIndex)
    }
    
    /**
     * 播放上一首歌曲
     * 
     * 如果当前是第一首，循环到最后一首（循环播放）
     */
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
    
    /**
     * 跳转到指定位置
     * 
     * @param position 目标位置（毫秒）
     */
    fun seekTo(position: Long) {
        try {
            exoPlayer?.seekTo(position)
            Log.d(TAG, "Seeked to: $position ms")
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking", e)
        }
    }
    
    /**
     * 获取歌曲总时长
     * 
     * @return 歌曲时长（毫秒），如果未播放则返回0
     */
    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0
    }
    
    /**
     * 获取当前播放位置
     * 
     * @return 当前位置（毫秒），如果未播放则返回0
     */
    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0
    }
    
    /**
     * 获取当前播放的歌曲
     * 
     * @return 当前歌曲的MusicMetadata对象，如果没有则返回null
     */
    fun getCurrentSong(): MusicMetadata? {
        return if (currentSongIndex >= 0 && currentSongIndex < songs.size) {
            songs[currentSongIndex]
        } else {
            null
        }
    }
    
    /**
     * 获取当前歌曲索引
     * 
     * @return 当前索引，如果没有歌曲播放则返回-1
     */
    fun getCurrentSongIndex(): Int {
        return currentSongIndex
    }
    
    /**
     * 设置播放速度
     * 
     * @param speed 播放速度（0.25f 到 4.0f）
     */
    fun setPlaybackSpeed(speed: Float) {
        try {
            exoPlayer?.setPlaybackSpeed(speed)
            Log.d(TAG, "Playback speed set to: $speed")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting playback speed", e)
        }
    }
    
    /**
     * 设置音量
     * 
     * @param volume 音量（0.0f 到 1.0f）
     */
    fun setVolume(volume: Float) {
        try {
            exoPlayer?.volume = volume.coerceIn(0f, 1f)
            Log.d(TAG, "Volume set to: $volume")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume", e)
        }
    }
    
    /**
     * 获取音量
     * 
     * @return 当前音量（0.0f 到 1.0f）
     */
    fun getVolume(): Float {
        return exoPlayer?.volume ?: 1f
    }
    
    /**
     * 释放资源
     * 
     * 当不再需要播放器时，必须调用此方法释放资源。
     * ExoPlayer会占用音频硬件资源，不释放会导致其他应用无法播放音频。
     */
    fun release() {
        try {
            exoPlayer?.release()
            exoPlayer = null
            currentSongIndex = -1
            playbackListener = null
            Log.d(TAG, "ExoPlayer released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing ExoPlayer", e)
        }
    }
    
    // ==================== 回调接口 ====================
    
    /**
     * 播放状态回调接口
     */
    interface PlaybackListener {
        /**
         * 歌曲改变时回调
         */
        fun onSongChanged(song: MusicMetadata, index: Int) {}
        
        /**
         * 播放状态改变时回调
         */
        fun onPlayingChanged(isPlaying: Boolean) {}
        
        /**
         * 播放器准备就绪时回调
         */
        fun onReady() {}
        
        /**
         * 歌曲播放结束时回调
         */
        fun onSongEnded() {}
        
        /**
         * 发生错误时回调
         */
        fun onError(message: String) {}
    }
}
