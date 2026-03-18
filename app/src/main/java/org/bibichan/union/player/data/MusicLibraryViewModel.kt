/**
 * MusicLibraryViewModel.kt - 音乐库视图模型
 *
 * 管理音乐库的状态和业务逻辑
 */
package org.bibichan.union.player.data

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 音乐库视图模型
 */
class MusicLibraryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context: Context = application
    private val musicScanner = MusicScanner(context)
    private val libraryManager = MusicLibraryManager(context)
    
    // 音乐库状态
    private val _musicLibrary = MutableStateFlow<List<MusicMetadata>>(emptyList())
    val musicLibrary: StateFlow<List<MusicMetadata>> = _musicLibrary.asStateFlow()
    
    // 播放列表状态
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()
    
    // 扫描状态
    private val _scanState = MutableStateFlow<MusicScanner.ScanState>(MusicScanner.ScanState.Idle)
    val scanState: StateFlow<MusicScanner.ScanState> = _scanState.asStateFlow()
    
    // 库统计信息
    private val _libraryStats = MutableStateFlow(LibraryStats(0, 0, 0, 0, 0))
    val libraryStats: StateFlow<LibraryStats> = _libraryStats.asStateFlow()
    
    // 选中的扫描目录
    private val _selectedScanDirectory = MutableStateFlow<String?>(null)
    val selectedScanDirectory: StateFlow<String?> = _selectedScanDirectory.asStateFlow()
    
    init {
        // 加载已保存的音乐库
        loadSavedLibrary()
        
        // 观察扫描状态
        viewModelScope.launch {
            musicScanner.scanState.collect { state ->
                _scanState.value = state
                
                // 如果扫描完成，更新音乐库
                if (state is MusicScanner.ScanState.Completed) {
                    updateLibrary(state.result)
                }
            }
        }
    }
    
    /**
     * 加载已保存的音乐库
     */
    private fun loadSavedLibrary() {
        viewModelScope.launch {
            val savedSongs = libraryManager.musicLibrary.value
            val savedPlaylists = libraryManager.playlists.value
            
            _musicLibrary.value = savedSongs
            _playlists.value = savedPlaylists
            _libraryStats.value = libraryManager.getLibraryStats()
        }
    }
    
    /**
     * 扫描目录
     */
    fun scanDirectory(directoryPath: String) {
        viewModelScope.launch {
            _selectedScanDirectory.value = directoryPath
            
            // 执行扫描
            val result = musicScanner.scanDirectory(directoryPath)
            
            // 解析播放列表文件
            val playlistFiles = findPlaylistFiles(directoryPath)
            val parsedPlaylists = mutableListOf<Playlist>()
            
            playlistFiles.forEach { playlistFile ->
                val playlist = musicScanner.parsePlaylistFile(
                    playlistFile,
                    result.songs
                )
                playlist?.let { parsedPlaylists.add(it) }
            }
            
            // 更新结果
            val finalResult = result.copy(playlists = parsedPlaylists)
            updateLibrary(finalResult)
        }
    }
    
    /**
     * 扫描URI（用于Storage Access Framework）
     */
    fun scanUri(uri: Uri) {
        viewModelScope.launch {
            try {
                val directoryPath = uri.path ?: return@launch
                scanDirectory(directoryPath)
            } catch (e: Exception) {
                _scanState.value = MusicScanner.ScanState.Error(
                    "Failed to scan URI: ${e.message}"
                )
            }
        }
    }
    
    /**
     * 更新音乐库
     */
    private fun updateLibrary(result: ScanResult) {
        viewModelScope.launch {
            // 更新音乐库
            _musicLibrary.value = result.songs
            libraryManager.updateMusicLibrary(result.songs)
            
            // 更新播放列表
            _playlists.value = result.playlists
            libraryManager.updatePlaylists(result.playlists)
            
            // 添加扫描目录
            _selectedScanDirectory.value?.let {
                libraryManager.addScanDirectory(it)
            }
            
            // 更新统计信息
            _libraryStats.value = libraryManager.getLibraryStats()
        }
    }
    
    /**
     * 查找播放列表文件
     */
    private suspend fun findPlaylistFiles(directoryPath: String): List<java.io.File> {
        val playlistFiles = mutableListOf<java.io.File>()
        val directory = java.io.File(directoryPath)
        
        if (directory.exists() && directory.isDirectory) {
            directory.walkTopDown()
                .filter { it.isFile }
                .filter { 
                    val ext = it.extension.lowercase(Locale.getDefault())
                    ext == "m3u" || ext == "m3u8"
                }
                .forEach { playlistFiles.add(it) }
        }
        
        return playlistFiles
    }
    
    /**
     * 搜索歌曲
     */
    fun searchSongs(query: String): List<MusicMetadata> {
        return libraryManager.searchSongs(query)
    }
    
    /**
     * 获取指定艺术家的所有歌曲
     */
    fun getSongsByArtist(artist: String): List<MusicMetadata> {
        return libraryManager.getSongsByArtist(artist)
    }
    
    /**
     * 获取指定专辑的所有歌曲
     */
    fun getSongsByAlbum(album: String): List<MusicMetadata> {
        return libraryManager.getSongsByAlbum(album)
    }
    
    /**
     * 清除音乐库
     */
    fun clearLibrary() {
        viewModelScope.launch {
            _musicLibrary.value = emptyList()
            _playlists.value = emptyList()
            _libraryStats.value = LibraryStats(0, 0, 0, 0, 0)
            libraryManager.clearLibrary()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        musicScanner.cleanup()
    }
}
