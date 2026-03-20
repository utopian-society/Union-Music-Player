/**
 * Album.kt - 专辑数据类
 *
 * 用于Library屏幕的专辑分组显示
 */
package org.bibichan.union.player.ui.library.data

import android.net.Uri
import org.bibichan.union.player.data.MusicMetadata
import java.util.Locale

/**
 * 专辑数据类
 *
 * @property id 专辑唯一标识符
 * @property title 专辑标题
 * @property artistName 艺术家名称
 * @property artworkUri 专辑封面URI
 * @property songCount 歌曲数量
 * @property totalDuration 总时长（毫秒）
 * @property songs 专辑中的歌曲列表
 */
data class Album(
    val id: String,
    val title: String,
    val artistName: String,
    val artworkUri: Uri?,
    val songCount: Int,
    val totalDuration: Long,
    val songs: List<MusicMetadata>
) {
    /**
     * 格式化的总时长（如 "45:32" 或 "3:45:12"）
     */
    val formattedDuration: String
        get() {
            val totalSeconds = totalDuration / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60

            return if (hours > 0) {
                String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format(Locale.US, "%d:%02d", minutes, seconds)
            }
        }

    /**
     * 年份信息（从第一首歌获取）
     */
    val year: Int?
        get() = songs.firstOrNull()?.year

    companion object {
        /**
         * 从MusicMetadata列表创建专辑
         *
         * 根据album字段分组，处理"Unknown Album"情况
         */
        fun fromSongs(songs: List<MusicMetadata>): List<Album> {
            if (songs.isEmpty()) return emptyList()

            // 按专辑名称分组
            val groupedByAlbum = songs.groupBy { song ->
                // 标准化专辑名称
                val albumName = song.album.trim()
                if (albumName.isBlank() || albumName.equals("Unknown Album", ignoreCase = true)) {
                    "Unknown Album"
                } else {
                    albumName
                }
            }

            return groupedByAlbum.map { (albumName, albumSongs) ->
                // 使用艺术家名称（取第一首歌的艺术家）
                val artistName = albumSongs.firstOrNull()?.artist?.takeIf { it.isNotBlank() }
                    ?: "Unknown Artist"

                // 生成专辑ID（使用专辑名+艺术家名作为唯一标识）
                val albumId = "${albumName}_${artistName}".hashCode().toString()

                // 获取专辑封面（第一首有封面的歌）
                val artworkUri = albumSongs
                    .firstOrNull { it.albumArt != null || it.albumArtPath != null }
                    ?.let { song ->
                        when {
                            song.albumArtPath != null -> Uri.parse(song.albumArtPath)
                            song.uri != Uri.EMPTY -> song.uri
                            else -> null
                        }
                    }

                // 计算总时长
                val totalDuration = albumSongs.sumOf { it.duration }

                // 按曲目号排序
                val sortedSongs = albumSongs.sortedBy { it.trackNumber ?: Int.MAX_VALUE }

                Album(
                    id = albumId,
                    title = albumName,
                    artistName = artistName,
                    artworkUri = artworkUri,
                    songCount = albumSongs.size,
                    totalDuration = totalDuration,
                    songs = sortedSongs
                )
            }.sortedBy { it.title } // 按专辑名排序
        }
    }
}
