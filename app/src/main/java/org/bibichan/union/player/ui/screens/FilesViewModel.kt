/**
 * FilesViewModel.kt - 檔案瀏覽器視圖模型
 *
 * 管理檔案瀏覽器的狀態，包括已掃描的資料夾列表和當前瀏覽路徑。
 * 2026-03-22: 新增功能，支援應用內檔案瀏覽器
 */
package org.bibichan.union.player.ui.screens

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.data.ScannedFilesManager
import org.bibichan.union.player.data.ScannedFolder

private const val TAG = "FilesViewModel"

/**
 * 檔案瀏覽器視圖模型
 *
 * 負責管理檔案瀏覽器的狀態：
 * - 已掃描的資料夾列表
 * - 當前瀏覽路徑
 * - 當前顯示的檔案列表
 */
class FilesViewModel(application: Application) : AndroidViewModel(application) {
    
    // ScannedFilesManager 實例
    private val scannedFilesManager = ScannedFilesManager.getInstance(application)
    
    // 已掃描的資料夾列表
    val scannedFolders: StateFlow<List<ScannedFolder>> = scannedFilesManager.scannedFolders
    
    // 當前瀏覽的資料夾 URI（null 表示根目錄）
    private val _currentFolderUri = MutableStateFlow<Uri?>(null)
    val currentFolderUri: StateFlow<Uri?> = _currentFolderUri.asStateFlow()
    
    // 當前資料夾名稱
    private val _currentFolderName = MutableStateFlow("Files")
    val currentFolderName: StateFlow<String> = _currentFolderName.asStateFlow()
    
    // 當前資料夾內的歌曲
    private val _currentSongs = MutableStateFlow<List<MusicMetadata>>(emptyList())
    val currentSongs: StateFlow<List<MusicMetadata>> = _currentSongs.asStateFlow()
    
    // 是否正在載入
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    // 導航歷史
    private val navigationHistory = mutableListOf<Uri?>()
    
    init {
        // 觀察已掃描資料夾的變化
        observeScannedFolders()
    }
    
    /**
     * 觀察已掃描資料夾的變化
     */
    private fun observeScannedFolders() {
        viewModelScope.launch {
            scannedFilesManager.scannedFolders.collect { folders ->
                Log.d(TAG, "Scanned folders updated: ${folders.size} folders")
                // 如果當前正在瀏覽某個資料夾，更新其內容
                _currentFolderUri.value?.let { uri ->
                    updateCurrentFolderContent(uri)
                }
            }
        }
    }
    
    /**
     * 導航到指定資料夾
     */
    fun navigateToFolder(folderUri: Uri) {
        Log.d(TAG, "Navigating to folder: $folderUri")
        
        // 保存當前位置到歷史
        _currentFolderUri.value?.let {
            navigationHistory.add(it)
        }
        
        _currentFolderUri.value = folderUri
        updateCurrentFolderContent(folderUri)
    }
    
    /**
     * 導航到根目錄
     */
    fun navigateToRoot() {
        Log.d(TAG, "Navigating to root")
        navigationHistory.clear()
        _currentFolderUri.value = null
        _currentFolderName.value = "Files"
        _currentSongs.value = emptyList()
    }
    
    /**
     * 返回上一級
     */
    fun navigateUp(): Boolean {
        return if (navigationHistory.isNotEmpty()) {
            val previousUri = navigationHistory.removeLast()
            Log.d(TAG, "Navigating up to: $previousUri")
            _currentFolderUri.value = previousUri
            if (previousUri != null) {
                updateCurrentFolderContent(previousUri)
            } else {
                _currentFolderName.value = "Files"
                _currentSongs.value = emptyList()
            }
            true
        } else if (_currentFolderUri.value != null) {
            // 從資料夾返回根目錄
            Log.d(TAG, "Navigating to root from folder")
            _currentFolderUri.value = null
            _currentFolderName.value = "Files"
            _currentSongs.value = emptyList()
            true
        } else {
            false
        }
    }
    
    /**
     * 更新當前資料夾內容
     */
    private fun updateCurrentFolderContent(folderUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val folder = scannedFolders.value.find { it.uri == folderUri }
                _currentFolderName.value = folder?.name ?: "Unknown Folder"
                
                // 獲取資料夾內的歌曲
                val songs = scannedFilesManager.getSongsInFolder(folderUri)
                _currentSongs.value = songs
                
                Log.d(TAG, "Updated folder content: ${songs.size} songs")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating folder content", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * 獲取指定資料夾的歌曲
     */
    fun getSongsForFolder(folderUri: Uri): List<MusicMetadata> {
        return scannedFilesManager.getSongsInFolder(folderUri)
    }
    
    /**
     * 移除已掃描的資料夾
     */
    fun removeFolder(folderUri: Uri) {
        Log.i(TAG, "Removing folder: $folderUri")
        scannedFilesManager.removeScannedFolder(folderUri)
        
        // 如果當前正在瀏覽被移除的資料夾，返回根目錄
        if (_currentFolderUri.value == folderUri) {
            navigateToRoot()
        }
    }
    
    /**
     * 是否可以返回上一級
     */
    fun canNavigateUp(): Boolean {
        return _currentFolderUri.value != null || navigationHistory.isNotEmpty()
    }
    
    /**
     * 獲取所有已掃描的歌曲
     */
    fun getAllScannedSongs(): List<MusicMetadata> {
        return scannedFilesManager.scannedSongs.value
    }
}
