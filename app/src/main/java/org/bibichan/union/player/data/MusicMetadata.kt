/**
 * MusicMetadata.kt - 音乐元数据数据类
 *
 * 包含歌曲的完整元数据信息，包括专辑封面、艺术家等
 */
package org.bibichan.union.player.data

import android.graphics.Bitmap
import android.net.Uri

/**
 * 音乐元数据
 *
 * @property id 唯一标识符
 * @property title 歌曲标题
 * @property artist 艺术家
 * @property album 专辑名称
 * @property duration 时长（毫秒）
 * @property filePath 文件路径
 * @property uri 文件URI
 * @property albumArt 专辑封面图片
 * @property albumArtPath 专辑封面路径（如果有）
 * @property genre 音乐类型
 * @property year 发行年份
 * @property trackNumber 曲目编号
 * @property format 音频格式（MP3, FLAC, ALAC等）
 */
data class MusicMetadata(
    val id: Long = 0,
    val title: String = "",
    val artist: String = "Unknown Artist",
    val album: String = "Unknown Album",
    val duration: Long = 0,
    val filePath: String = "",
    val uri: Uri = Uri.EMPTY,
    val albumArt: Bitmap? = null,
    val albumArtPath: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val format: AudioFormat = AudioFormat.MP3
)

/**
 * 音频格式枚举
 */
enum class AudioFormat(val extension: String, val displayName: String) {
    MP3("mp3", "MP3"),
    FLAC("flac", "FLAC"),
    ALAC("m4a", "ALAC"), // ALAC通常使用.m4a容器
    M4A("m4a", "M4A"),
    AAC("aac", "AAC"),
    WAV("wav", "WAV"),
    OGG("ogg", "OGG"),
    UNKNOWN("", "Unknown");
    
    companion object {
        /**
         * 根据文件扩展名获取音频格式
         */
        fun fromExtension(extension: String): AudioFormat {
            return values().find { it.extension.equals(extension, ignoreCase = true) }
                ?: UNKNOWN
        }
        
        /**
         * 支持的音频格式扩展名列表
         */
        val supportedExtensions = listOf("mp3", "flac", "m4a", "aac", "wav", "ogg")
    }
}

/**
 * 播放列表
 *
 * @property id 播放列表ID
 * @property name 播放列表名称
 * @property songs 歌曲列表
 * @property filePath 播放列表文件路径（对于M3U8文件）
 * @property creationDate 创建日期
 */
data class Playlist(
    val id: Long = 0,
    val name: String,
    val songs: List<MusicMetadata> = emptyList(),
    val filePath: String? = null,
    val creationDate: Long = System.currentTimeMillis()
)

/**
 * 扫描结果
 *
 * @property songs 扫描到的歌曲列表
 * @property playlists 扫描到的播放列表
 * @property scanTime 扫描耗时（毫秒）
 * @property scannedDirectories 扫描的目录数量
 * @property scannedFiles 扫描的文件数量
 */
data class ScanResult(
    val songs: List<MusicMetadata> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val scanTime: Long = 0,
    val scannedDirectories: Int = 0,
    val scannedFiles: Int = 0
)
