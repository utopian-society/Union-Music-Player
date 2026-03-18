/**
 * MusicMetadataExtensions.kt - 数据类扩展函数
 *
 * 为数据类添加JSON序列化方法
 */
package org.bibichan.union.player.data

import android.net.Uri
import org.json.JSONObject

/**
 * MusicMetadata的JSON扩展函数
 */
fun MusicMetadata.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("title", title)
        put("artist", artist)
        put("album", album)
        put("duration", duration)
        put("filePath", filePath)
        put("uri", uri.toString())
        put("albumArtPath", albumArtPath ?: "")
        put("genre", genre ?: "")
        put("year", year ?: 0)
        put("trackNumber", trackNumber ?: 0)
        put("format", format.name)
    }
}

fun MusicMetadata.Companion.fromJson(json: JSONObject): MusicMetadata {
    return MusicMetadata(
        id = json.getLong("id"),
        title = json.getString("title"),
        artist = json.getString("artist"),
        album = json.getString("album"),
        duration = json.getLong("duration"),
        filePath = json.getString("filePath"),
        uri = Uri.parse(json.getString("uri")),
        albumArtPath = if (json.getString("albumArtPath").isNotEmpty()) 
            json.getString("albumArtPath") else null,
        genre = if (json.getString("genre").isNotEmpty()) 
            json.getString("genre") else null,
        year = if (json.getInt("year") != 0) json.getInt("year") else null,
        trackNumber = if (json.getInt("trackNumber") != 0) 
            json.getInt("trackNumber") else null,
        format = AudioFormat.valueOf(json.getString("format"))
    )
}

/**
 * Playlist的JSON扩展函数
 */
fun Playlist.toJson(): JSONObject {
    return JSONObject().apply {
        put("id", id)
        put("name", name)
        put("filePath", filePath ?: "")
        put("creationDate", creationDate)
        
        val songsArray = org.json.JSONArray()
        songs.forEach { song ->
            songsArray.put(song.toJson())
        }
        put("songs", songsArray)
    }
}

fun Playlist.Companion.fromJson(json: JSONObject): Playlist {
    val songsArray = json.getJSONArray("songs")
    val songs = mutableListOf<MusicMetadata>()
    for (i in 0 until songsArray.length()) {
        songs.add(MusicMetadata.fromJson(songsArray.getJSONObject(i)))
    }
    
    return Playlist(
        id = json.getLong("id"),
        name = json.getString("name"),
        songs = songs,
        filePath = if (json.getString("filePath").isNotEmpty()) 
            json.getString("filePath") else null,
        creationDate = json.getLong("creationDate")
    )
}
