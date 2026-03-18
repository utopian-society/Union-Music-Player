/**
 * MusicPlayer.kt - 音乐播放器封装类
 * 
 * 这个类封装了Android的MediaPlayer，提供更简洁的API。
 * MediaPlayer是Android内置的媒体播放器，支持多种音频格式。
 * 
 * 学习要点：
 * 1. MediaPlayer的生命周期管理
 * 2. 错误处理和资源释放
 * 3. 类的封装设计
 */

package org.bibichan.union.player

import android.content.Context          // 提供访问应用资源和系统服务的接口
import android.media.MediaPlayer       // Android内置的媒体播放器
import android.util.Log                // 日志工具

/**
 * MusicPlayer类 - 音乐播放器封装
 * 
 * 这个类封装了MediaPlayer的复杂性，提供简单易用的方法。
 * 使用private访问修饰符隐藏内部实现细节。
 * 
 * @param context 上下文对象，用于创建MediaPlayer
 */
class MusicPlayer(private val context: Context) {
    
    // ==================== 私有属性 ====================
    
    /**
     * MediaPlayer实例 - Android内置的媒体播放器
     * 
     * 类型为可空（?），因为MediaPlayer可能尚未创建或已被释放
     * 使用可空类型需要注意：
     * 1. 访问时需要使用?.（安全调用）或!!（非空断言）
     * 2. 每次使用前检查是否为null
     */
    private var mediaPlayer: MediaPlayer? = null
    
    /**
     * 当前播放歌曲的索引
     * 初始值为-1，表示没有歌曲在播放
     */
    private var currentSongIndex: Int = -1
    
    /**
     * 歌曲列表
     * 使用List<Song>类型，表示不可变的歌曲列表
     */
    private var songs: List<Song> = emptyList()
    
    /**
     * 日志标签，用于调试
     */
    private val TAG = "MusicPlayer"
    
    // ==================== 公共方法 ====================
    
    /**
     * 设置歌曲列表
     * 
     * @param songs 歌曲列表（List类型）
     */
    fun setSongs(songs: List<Song>) {
        this.songs = songs
        Log.d(TAG, "Songs set: ${songs.size} songs")
    }
    
    /**
     * 播放指定索引的歌曲
     * 
     * 播放流程：
     * 1. 检查索引是否有效
     * 2. 释放之前的MediaPlayer
     * 3. 创建新的MediaPlayer
     * 4. 开始播放
     * 
     * @param songIndex 歌曲在列表中的索引（从0开始）
     */
    fun play(songIndex: Int) {
        // 检查索引是否有效
        // ||：逻辑或运算符，只要有一个条件为真，结果就为真
        if (songIndex < 0 || songIndex >= songs.size) {
            Log.e(TAG, "Invalid song index: $songIndex")
            return  // 提前返回，不执行后续代码
        }
        
        try {
            // 更新当前歌曲索引
            currentSongIndex = songIndex
            
            // 获取要播放的歌曲
            val song = songs[currentSongIndex]
            
            // 释放之前的MediaPlayer
            // ?.：安全调用操作符，如果mediaPlayer不为null则调用release()
            // 这样可以避免NullPointerException（空指针异常）
            mediaPlayer?.release()
            
            // 创建新的MediaPlayer
            // MediaPlayer.create()是静态工厂方法，返回一个配置好的MediaPlayer实例
            // 参数1：Context，用于访问资源
            // 参数2：资源ID（R.raw.xxx）
            mediaPlayer = MediaPlayer.create(context, song.resourceId)
            
            // 设置错误监听器
            // 这样可以在播放出错时得到通知，而不是直接崩溃
            mediaPlayer?.setOnErrorListener { mp, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                // 返回true表示我们处理了这个错误，false表示未处理
                true
            }
            
            // 开始播放
            // ?.start()：如果不为null则调用start()方法
            mediaPlayer?.start()
            
            Log.d(TAG, "Playing: ${song.title}")
            
        } catch (e: Exception) {
            // 捕获所有异常并记录
            Log.e(TAG, "Error playing song", e)
        }
    }
    
    /**
     * 暂停播放
     * 
     * 暂停后可以调用resume()恢复播放
     */
    fun pause() {
        try {
            // 检查是否正在播放
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                Log.d(TAG, "Paused")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing", e)
        }
    }
    
    /**
     * 恢复播放
     * 
     * 在暂停后调用，继续播放当前位置
     */
    fun resume() {
        try {
            // 检查MediaPlayer是否存在且未在播放
            if (mediaPlayer != null && !isPlaying()) {
                mediaPlayer?.start()
                Log.d(TAG, "Resumed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming", e)
        }
    }
    
    /**
     * 检查是否正在播放
     * 
     * @return true表示正在播放，false表示未播放或MediaPlayer未初始化
     */
    fun isPlaying(): Boolean {
        // Elvis操作符（?:）：
        // 如果左边的表达式不为null，返回左边的结果
        // 如果为null，返回右边的默认值（这里是false）
        return mediaPlayer?.isPlaying ?: false
    }
    
    /**
     * 播放下一首歌曲
     * 
     * 如果当前是最后一首，循环到第一首（循环播放）
     */
    fun next() {
        // 如果歌曲列表为空，直接返回
        if (songs.isEmpty()) {
            Log.e(TAG, "No songs in playlist")
            return
        }
        
        // 计算下一首歌曲的索引
        // %：取模运算符，用于循环（当currentSongIndex + 1 = songs.size时，结果为0）
        val nextSongIndex = (currentSongIndex + 1) % songs.size
        
        // 播放下一首
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
        
        // 计算上一首歌曲的索引
        // if表达式：Kotlin中if是表达式，可以返回值
        // 如果currentSongIndex - 1 < 0，返回songs.size - 1（最后一首）
        // 否则返回currentSongIndex - 1
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
     * @param progress 目标位置（毫秒）
     */
    fun seekTo(progress: Int) {
        try {
            // 确保MediaPlayer存在且在播放
            if (mediaPlayer != null) {
                mediaPlayer?.seekTo(progress)
                Log.d(TAG, "Seeked to: $progress ms")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error seeking", e)
        }
    }
    
    /**
     * 获取歌曲总时长
     * 
     * @return 歌曲时长（毫秒），如果未播放则返回0
     */
    fun getDuration(): Int {
        // 使用Elvis操作符提供默认值0
        return mediaPlayer?.duration ?: 0
    }
    
    /**
     * 获取当前播放位置
     * 
     * @return 当前位置（毫秒），如果未播放则返回0
     */
    fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }
    
    /**
     * 获取当前播放的歌曲
     * 
     * @return 当前歌曲的Song对象，如果没有则返回null
     */
    fun getCurrentSong(): Song? {
        // 检查索引是否有效
        return if (currentSongIndex >= 0 && currentSongIndex < songs.size) {
            songs[currentSongIndex]
        } else {
            null
        }
    }
    
    /**
     * 释放资源
     * 
     * 当不再需要播放器时，必须调用此方法释放资源。
     * MediaPlayer会占用音频硬件资源，不释放会导致其他应用无法播放音频。
     */
    fun release() {
        try {
            // 释放MediaPlayer资源
            mediaPlayer?.release()
            // 将引用设为null，帮助垃圾回收
            mediaPlayer = null
            // 重置索引
            currentSongIndex = -1
            
            Log.d(TAG, "MediaPlayer released")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaPlayer", e)
        }
    }
}
