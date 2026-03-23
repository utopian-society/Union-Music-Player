/**
 * LibraryViewModel.kt - 音樂庫視圖模型
 *
 * 管理專輯分組的數據和狀態，優化性能
 * 2026-03-22: 修改為從 ScannedFilesManager 獲取數據，不再自動掃描
 */
package org.bibichan.union.player.ui.library

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.data.ScannedFilesManager
import org.bibichan.union.player.ui.library.data.Album
import org.bibichan.union.player.ui.PermissionManager

/**
 * Artist Data Class - 用於顯示藝術家信息
 */
data class ArtistData(
    val name: String,
    val albumCount: Int,
    val artworkUri: Uri?
)

/**
 * 音樂庫視圖模型
 *
 * 負責從 ScannedFilesManager 獲取已掃描的音樂數據，並按專輯分組
 * 優化：使用 StateFlow 緩存計算結果，避免主線程重計算
 */
class LibraryViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "LibraryViewModel"

    // ScannedFilesManager 實例
    private val scannedFilesManager = ScannedFilesManager.getInstance(application)

    // 專輯列表狀態
    private val _albums = MutableStateFlow<List<Album>>(emptyList())
    val albums: StateFlow<List<Album>> = _albums.asStateFlow()

    // 加載狀態
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 錯誤狀態
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 是否有權限（從 PermissionManager 獲取）
    val hasPermission = PermissionManager.permissionGranted

    // 是否有已掃描的資料夾
    val hasScannedFolders: StateFlow<Boolean> = scannedFilesManager.hasScannedFolders

    /**
     * 優化：緩存 topArtists 計算結果
     * 使用 map 和 stateIn 避免重複計算
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
        // 初始化時檢查權限狀態
        PermissionManager.updatePermissionStatus(getApplication())
        
        // 觀察 ScannedFilesManager 的數據變化
        observeScannedFiles()
    }

    /**
     * 觀察 ScannedFilesManager 的變化
     * 當有新的掃描結果時自動更新 Library
     */
    private fun observeScannedFiles() {
        viewModelScope.launch {
            scannedFilesManager.scannedSongs.collect { songs ->
                Log.d(TAG, "Received ${songs.size} songs from ScannedFilesManager")
                
                if (songs.isNotEmpty()) {
                    // 將掃描的歌曲轉換為專輯列表
                    val albumList = withContext(Dispatchers.Default) {
                        Album.fromSongs(songs)
                    }
                    _albums.value = albumList
                    Log.i(TAG, "Updated library with ${albumList.size} albums")
                } else {
                    _albums.value = emptyList()
                }
                
                _isLoading.value = false
            }
        }
    }

    /**
     * 公開方法：檢查並更新權限狀態
     * 可在權限授予後調用以刷新 UI
     */
    fun checkAndUpdatePermissionStatus() {
        PermissionManager.updatePermissionStatus(getApplication())
        Log.d(TAG, "Permission status updated: ${hasPermission.value}")
    }

    /**
     * 刷新音樂庫
     * 從 ScannedFilesManager 重新獲取數據（實際上 Flow 會自動更新）
     */
    fun refresh() {
        _isLoading.value = true
        // ScannedFilesManager 的 Flow 會自動觸發更新
        // 這裡只是設置加載狀態，實際數據會通過 observeScannedFiles 更新
        viewModelScope.launch {
            // 短暫延遲以顯示加載狀態
            kotlinx.coroutines.delay(500)
            _isLoading.value = false
        }
    }

    /**
     * 按標題搜索專輯
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
