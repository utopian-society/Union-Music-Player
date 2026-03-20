/**
 * LibraryViewModel.kt - 音乐库视图模型
 * 
 * 管理专辑分组的数据和状态，优化性能
 */
package org.bibichan.union.player.ui.library

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.ui.library.data.Album

/**
 * Artist Data Class - 用于显示艺术家信息
 */
data class ArtistData(
    val name: String,
    val albumCount: Int,
    val artworkUri: Uri?
)

/**
 * 音乐库视图模型
 * 
 * 负责从系统MediaStore加载音乐数据，并按专辑分组
 * 优化：使用StateFlow缓存计算结果，避免主线程重计算
 */
class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "LibraryViewModel"
    
    // 专辑列表状态
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()
    
    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 错误状态
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // 是否有权限
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()
    
    /**
     * 优化：缓存topArtists计算结果
     * 使用map和stateIn避免重复计算
     */
    val topArtists: StateFlow<List<ArtistData>> = _albums
        .map { albumList ->
            albumList
                .groupBy { it.artistName }
                .map { (artist, artistAlbums) ->
                    ArtistData(
                        name = artist,
                        albumCount = artistAlbums.size,
                        artworkUri = artistAlbums.firstOrNull()?.artworkUri
                    )
                }
                .sortedByDescending { it.albumCount }
                .take(10)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    
    init {
        loadMusicLibrary()
    }
    
    /**
     * 加载音乐库
     */
    fun loadMusicLibrary() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // 检查权限
                val hasPerm = checkStoragePermission()
                _hasPermission.value = hasPerm
                
                if (!hasPerm) {
                    _error.value = "Storage permission required"
                    _albums.value = emptyList()
                    _isLoading.value = false
                    return@launch
                }
                
                // 在后台线程加载音乐
                val songs = withContext(Dispatchers.IO) {
                    loadSongsFromMediaStore()
                }
                
                Log.i(TAG, "Loaded ${songs.size} songs")
                
                // 按专辑分组
                val albumList = withContext(Dispatchers.Default) {
                    Album.fromSongs(songs)
                }
                
                Log.i(TAG, "Grouped into ${albumList.size} albums")
                _albums.value = albumList
            } catch (e: Exception) {
                Log.e(TAG, "Error loading music library", e)
                _error.value = "Failed to load music: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 检查存储权限
     */
    private fun checkStoragePermission(): Boolean {
        val context = getApplication<Application>()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_AUDIO) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == 
                android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * 从MediaStore加载歌曲
     */
    private fun loadSongsFromMediaStore(): List<MusicMetadata> {
        val context = getApplication<Application>()
        val songs = mutableListOf<MusicMetadata>()
        
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM_ID
            )
            
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} = 1"
            val sortOrder = "${MediaStore.Audio.Media.ALBUM} ASC, ${MediaStore.Audio.Media.TRACK} ASC"
            
            val cursor: Cursor? = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )
            
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val yearColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                
                while (it.moveToNext()) {
                    val id = it.getLong(idColumn)
                    val title = it.getString(titleColumn) ?: "Unknown Title"
                    val artist = it.getString(artistColumn)?.takeIf { s -> s.isNotBlank() } ?: "Unknown Artist"
                    val album = it.getString(albumColumn)?.takeIf { s -> s.isNotBlank() } ?: "Unknown Album"
                    val duration = it.getLong(durationColumn)
                    val track = it.getInt(trackColumn)
                    val year = it.getInt(yearColumn)
                    val data = it.getString(dataColumn) ?: ""
                    val albumId = it.getLong(albumIdColumn)
                    
                    // 构建歌曲URI
                    val songUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    
                    // 构建专辑封面URI
                    val albumArtUri = ContentUris.withAppendedId(
                        Uri.parse("content://media/external/audio/albumart"),
                        albumId
                    )
                    
                    val metadata = MusicMetadata(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        filePath = data,
                        uri = songUri,
                        albumArtPath = albumArtUri.toString(),
                        year = year.takeIf { y -> y > 0 },
                        trackNumber = track.takeIf { t -> t > 0 }
                    )
                    
                    songs.add(metadata)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
        }
        
        return songs
    }
    
    /**
     * 刷新音乐库
     */
    fun refresh() {
        loadMusicLibrary()
    }
    
    /**
     * 按标题搜索专辑
     */
    fun searchAlbums(query: String): List<Album> {
        if (query.isBlank()) return _albums.value
        
        val lowerQuery = query.lowercase()
        return _albums.value.filter { album ->
            album.title.lowercase().contains(lowerQuery) ||
            album.artistName.lowercase().contains(lowerQuery)
        }
    }
}
