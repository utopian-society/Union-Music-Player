/**
 * MusicLibraryManager.kt - 音乐库管理器
 *
 * 管理音乐库的持久化存储和检索
 */
package org.bibichan.union.player.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * 音乐库管理器
 *
 * 负责音乐库的持久化存储、检索和管理
 */
class MusicLibraryManager(private val context: Context) {
    
    private val TAG = "MusicLibraryManager"
    
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("music_library", Context.MODE_PRIVATE)
    
    // 音乐库状态
    private val _musicLibrary = MutableStateFlow<List<MusicMetadata>>(emptyList())
    val musicLibrary: StateFlow<List<MusicMetadata>> = _musicLibrary.asStateFlow()
    
    // 播放列表状态
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()
    
    // 扫描目录历史
    private val _scanDirectories = MutableStateFlow<List<String>>(emptyList())
    val scanDirectories: StateFlow<List<String>> = _scanDirectories.asStateFlow()
    
    init {
        loadFromPrefs()
    }
    
    /**
     * 更新音乐库
     */
    fun updateMusicLibrary(songs: List<MusicMetadata>) {
        _musicLibrary.value = songs
        saveToPrefs()
    }
    
    /**
     * 更新播放列表
     */
    fun updatePlaylists(playlists: List<Playlist>) {
        _playlists.value = playlists
        saveToPrefs()
    }
    
    /**
     * 添加扫描目录
     */
    fun addScanDirectory(directory: String) {
        val currentDirs = _scanDirectories.value.toMutableList()
        if (!currentDirs.contains(directory)) {
            currentDirs.add(directory)
            _scanDirectories.value = currentDirs
            saveToPrefs()
        }
    }
    
    /**
     * 移除扫描目录
     */
    fun removeScanDirectory(directory: String) {
        val currentDirs = _scanDirectories.value.toMutableList()
        currentDirs.remove(directory)
        _scanDirectories.value = currentDirs
        saveToPrefs()
    }
    
    /**
     * 搜索歌曲
     */
    fun searchSongs(query: String): List<MusicMetadata> {
        val lowerQuery = query.lowercase()
        return _musicLibrary.value.filter { song ->
            song.title.lowercase().contains(lowerQuery) ||
            song.artist.lowercase().contains(lowerQuery) ||
            song.album.lowercase().contains(lowerQuery)
        }
    }
    
    /**
     * 获取所有艺术家
     */
    fun getAllArtists(): List<String> {
        return _musicLibrary.value.map { it.artist }.distinct().sorted()
    }
    
    /**
     * 获取所有专辑
     */
    fun getAllAlbums(): List<String> {
        return _musicLibrary.value.map { it.album }.distinct().sorted()
    }
    
    /**
     * 按艺术家分组
     */
    fun getSongsByArtist(artist: String): List<MusicMetadata> {
        return _musicLibrary.value.filter { it.artist == artist }
    }
    
    /**
     * 按专辑分组
     */
    fun getSongsByAlbum(album: String): List<MusicMetadata> {
        return _musicLibrary.value.filter { it.album == album }
    }
    
    /**
     * 保存到SharedPreferences
     */
    private fun saveToPrefs() {
        try {
            val editor = prefs.edit()
            
            // 保存歌曲列表
            val songsJson = JSONArray()
            _musicLibrary.value.forEach { song ->
                songsJson.put(song.toJson())
            }
            editor.putString("songs", songsJson.toString())
            
            // 保存播放列表
            val playlistsJson = JSONArray()
            _playlists.value.forEach { playlist ->
                playlistsJson.put(playlist.toJson())
            }
            editor.putString("playlists", playlistsJson.toString())
            
            // 保存扫描目录
            val dirsJson = JSONArray()
            _scanDirectories.value.forEach { dir ->
                dirsJson.put(dir)
            }
            editor.putString("scan_directories", dirsJson.toString())
            
            editor.apply()
            
            Log.d(TAG, "Saved ${_musicLibrary.value.size} songs and ${_playlists.value.size} playlists")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to prefs", e)
        }
    }
    
    /**
     * 从SharedPreferences加载
     */
    private fun loadFromPrefs() {
        try {
            // 加载歌曲
            val songsJson = prefs.getString("songs", null)
            if (songsJson != null) {
                val songsArray = JSONArray(songsJson)
                val songs = mutableListOf<MusicMetadata>()
                for (i in 0 until songsArray.length()) {
                    songs.add(MusicMetadata.fromJson(songsArray.getJSONObject(i)))
                }
                _musicLibrary.value = songs
            }
            
            // 加载播放列表
            val playlistsJson = prefs.getString("playlists", null)
            if (playlistsJson != null) {
                val playlistsArray = JSONArray(playlistsJson)
                val playlists = mutableListOf<Playlist>()
                for (i in 0 until playlistsArray.length()) {
                    playlists.add(Playlist.fromJson(playlistsArray.getJSONObject(i)))
                }
                _playlists.value = playlists
            }
            
            // 加载扫描目录
            val dirsJson = prefs.getString("scan_directories", null)
            if (dirsJson != null) {
                val dirsArray = JSONArray(dirsJson)
                val dirs = mutableListOf<String>()
                for (i in 0 until dirsArray.length()) {
                    dirs.add(dirsArray.getString(i))
                }
                _scanDirectories.value = dirs
            }
            
            Log.d(TAG, "Loaded ${_musicLibrary.value.size} songs and ${_playlists.value.size} playlists")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading from prefs", e)
        }
    }
    
    /**
     * 清除音乐库
     */
    fun clearLibrary() {
        _musicLibrary.value = emptyList()
        _playlists.value = emptyList()
        _scanDirectories.value = emptyList()
        saveToPrefs()
    }
    
    /**
     * 导出音乐库到JSON文件
     */
    fun exportToJson(): String {
        val root = JSONObject()
        val songsArray = JSONArray()
        _musicLibrary.value.forEach { song ->
            songsArray.put(song.toJson())
        }
        root.put("songs", songsArray)
        
        val playlistsArray = JSONArray()
        _playlists.value.forEach { playlist ->
            playlistsArray.put(playlist.toJson())
        }
        root.put("playlists", playlistsArray)
        
        return root.toString(2)
    }
    
    /**
     * 获取音乐库统计信息
     */
    fun getLibraryStats(): LibraryStats {
        val songs = _musicLibrary.value
        val totalDuration = songs.sumOf { it.duration }
        
        return LibraryStats(
            totalSongs = songs.size,
            totalArtists = songs.map { it.artist }.distinct().size,
            totalAlbums = songs.map { it.album }.distinct().size,
            totalDuration = totalDuration,
            totalPlaylists = _playlists.value.size
        )
    }
}

/**
 * 音乐库统计信息
 */
data class LibraryStats(
    val totalSongs: Int,
    val totalArtists: Int,
    val totalAlbums: Int,
    val totalDuration: Long, // 毫秒
    val totalPlaylists: Int
)
