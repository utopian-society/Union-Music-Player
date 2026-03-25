/**
 * MusicMetadata.kt - 音乐元数据数据类
 *
 * 包含歌曲的完整元数据信息，包括专辑封面、艺术家等
 */
package org.bibichan.union.player.data

import android.graphics.Bitmap
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

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
 * @property relativePath 相对路径（用于 SAF 扫描的目录结构）
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
    val relativePath: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val trackNumber: Int? = null,
    val format: AudioFormat = AudioFormat.MP3
) {
    /**
     * 将音乐元数据转换为JSON对象
     * 注意：albumArt (Bitmap) 不被序列化，因为Bitmap无法直接JSON化
     */
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("title", title)
            put("artist", artist)
            put("album", album)
            put("duration", duration)
            put("filePath", filePath)
            put("uri", uri.toString())
            put("albumArtPath", albumArtPath)
            put("relativePath", relativePath)
            put("genre", genre)
            put("year", year)
            put("trackNumber", trackNumber)
            put("format", format.name)
        }
    }

    companion object {
        /**
         * 从JSON对象创建音乐元数据
         */
        fun fromJson(json: JSONObject): MusicMetadata {
            return MusicMetadata(
                id = json.optLong("id", 0),
                title = json.optString("title", ""),
                artist = json.optString("artist", "Unknown Artist"),
                album = json.optString("album", "Unknown Album"),
                duration = json.optLong("duration", 0),
                filePath = json.optString("filePath", ""),
                uri = Uri.parse(json.optString("uri", "")),
                albumArtPath = json.optString("albumArtPath").takeIf { it.isNotEmpty() },
                relativePath = json.optString("relativePath").takeIf { it.isNotEmpty() },
                genre = json.optString("genre").takeIf { it.isNotEmpty() },
                year = json.optInt("year", 0).takeIf { it != 0 },
                trackNumber = json.optInt("trackNumber", 0).takeIf { it != 0 },
                format = try {
                    AudioFormat.valueOf(json.optString("format", "MP3"))
                } catch (e: IllegalArgumentException) {
                    AudioFormat.MP3
                }
            )
        }
    }
}

/**
 * 音频格式枚举
 */
enum class AudioFormat(val extension: String, val displayName: String) {
    MP3("mp3", "MP3"),
    FLAC("flac", "FLAC"),
    ALAC("m4a", "ALAC"),  // ALAC通常使用.m4a容器
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
            return values().find { it.extension.equals(extension, ignoreCase = true) } ?: UNKNOWN
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
) {
    /**
     * 将播放列表转换为JSON对象
     */
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            val songsArray = JSONArray()
            songs.forEach { song ->
                songsArray.put(song.toJson())
            }
            put("songs", songsArray)
            put("filePath", filePath)
            put("creationDate", creationDate)
        }
    }

    companion object {
        /**
         * 从JSON对象创建播放列表
         */
        fun fromJson(json: JSONObject): Playlist {
            val songsArray = json.optJSONArray("songs")
            val songs = mutableListOf<MusicMetadata>()
            if (songsArray != null) {
                for (i in 0 until songsArray.length()) {
                    songs.add(MusicMetadata.fromJson(songsArray.getJSONObject(i)))
                }
            }
            return Playlist(
                id = json.optLong("id", 0),
                name = json.optString("name", ""),
                songs = songs,
                filePath = json.optString("filePath").takeIf { it.isNotEmpty() },
                creationDate = json.optLong("creationDate", System.currentTimeMillis())
            )
        }
    }
}

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
