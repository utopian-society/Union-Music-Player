/**
 * FilesViewModel.kt - 檔案瀏覽器視圖模型
 *
 * 管理檔案瀏覽器的狀態，包括已掃描的資料夾列表與目錄層級。
 * 2026-03-24: 增加目錄層級導航，提供更接近檔案管理器的體驗
 */
package org.bibichan.union.player.ui.screens

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.data.ScannedFilesManager
import org.bibichan.union.player.data.ScannedFolder

private const val TAG = "FilesViewModel"

data class DirectoryItem(
    val name: String,
    val songCount: Int
)

data class DirectoryContents(
    val directories: List<DirectoryItem> = emptyList(),
    val songs: List<MusicMetadata> = emptyList()
)

/**
 * 檔案瀏覽器視圖模型
 */
class FilesViewModel(application: Application) : AndroidViewModel(application) {

    private val scannedFilesManager = ScannedFilesManager.getInstance(application)

    val scannedFolders: StateFlow<List<ScannedFolder>> = scannedFilesManager.scannedFolders

    private val _currentFolderUri = MutableStateFlow<Uri?>(null)
    val currentFolderUri: StateFlow<Uri?> = _currentFolderUri.asStateFlow()

    private val _currentFolderName = MutableStateFlow("Files")
    val currentFolderName: StateFlow<String> = _currentFolderName.asStateFlow()

    private val _currentPathSegments = MutableStateFlow<List<String>>(emptyList())
    val currentPathSegments: StateFlow<List<String>> = _currentPathSegments.asStateFlow()

    private val _currentDirectoryContents = MutableStateFlow(DirectoryContents())
    val currentDirectoryContents: StateFlow<DirectoryContents> = _currentDirectoryContents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeScannedFolders()
    }

    private fun observeScannedFolders() {
        viewModelScope.launch {
            scannedFilesManager.scannedFolders.collect { folders ->
                Log.d(TAG, "Scanned folders updated: ${folders.size} folders")
                _currentFolderUri.value?.let { uri ->
                    updateCurrentFolderContent(uri)
                }
            }
        }
    }

    fun navigateToFolder(folderUri: Uri) {
        Log.d(TAG, "Navigating to folder: $folderUri")
        _currentFolderUri.value = folderUri
        _currentPathSegments.value = emptyList()
        updateCurrentFolderContent(folderUri)
    }

    fun navigateToDirectory(directoryName: String) {
        _currentFolderUri.value?.let { uri ->
            _currentPathSegments.value = _currentPathSegments.value + directoryName
            updateCurrentFolderContent(uri)
        }
    }

    fun navigateToRoot() {
        Log.d(TAG, "Navigating to root")
        _currentFolderUri.value = null
        _currentFolderName.value = "Files"
        _currentPathSegments.value = emptyList()
        _currentDirectoryContents.value = DirectoryContents()
    }

    fun navigateUp(): Boolean {
        val folderUri = _currentFolderUri.value
        return if (folderUri == null) {
            false
        } else if (_currentPathSegments.value.isNotEmpty()) {
            _currentPathSegments.value = _currentPathSegments.value.dropLast(1)
            updateCurrentFolderContent(folderUri)
            true
        } else {
            navigateToRoot()
            true
        }
    }

    fun canNavigateUp(): Boolean {
        return _currentFolderUri.value != null
    }

    fun removeFolder(folderUri: Uri) {
        Log.i(TAG, "Removing folder: $folderUri")
        scannedFilesManager.removeScannedFolder(folderUri)
        if (_currentFolderUri.value == folderUri) {
            navigateToRoot()
        }
    }

    private fun updateCurrentFolderContent(folderUri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val folder = scannedFolders.value.find { it.uri == folderUri }
                val baseName = folder?.name ?: "Unknown Folder"
                val suffix = _currentPathSegments.value.joinToString(" / ")
                _currentFolderName.value = if (suffix.isBlank()) baseName else "$baseName / $suffix"

                val songs = scannedFilesManager.getSongsInFolder(folderUri)
                _currentDirectoryContents.value = buildDirectoryContents(baseName, songs, _currentPathSegments.value)

                Log.d(TAG, "Updated folder content: ${songs.size} songs")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating folder content", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun buildDirectoryContents(
        rootName: String,
        songs: List<MusicMetadata>,
        currentPath: List<String>
    ): DirectoryContents {
        val directories = mutableMapOf<String, Int>()
        val songsInCurrent = mutableListOf<MusicMetadata>()

        for (song in songs) {
            val pathSegments = resolvePathSegments(song, rootName)
            if (!startsWith(pathSegments, currentPath)) {
                continue
            }

            val remaining = pathSegments.drop(currentPath.size)
            if (remaining.size <= 1) {
                songsInCurrent.add(song)
            } else {
                val dirName = remaining.first()
                directories[dirName] = (directories[dirName] ?: 0) + 1
            }
        }

        val directoryItems = directories.entries
            .sortedBy { it.key.lowercase() }
            .map { DirectoryItem(name = it.key, songCount = it.value) }

        val sortedSongs = songsInCurrent.sortedBy { it.title.lowercase() }

        return DirectoryContents(
            directories = directoryItems,
            songs = sortedSongs
        )
    }

    private fun resolvePathSegments(song: MusicMetadata, rootName: String): List<String> {
        val relativePath = song.relativePath?.takeIf { it.isNotBlank() }
        if (relativePath != null) {
            val normalized = relativePath.replace("\\", "/").trim('/')
            if (normalized.isNotBlank()) {
                return normalized.split("/").filter { it.isNotBlank() }
            }
        }

        val rawPath = when {
            song.filePath.isNotBlank() -> song.filePath
            song.uri != Uri.EMPTY -> song.uri.path ?: song.uri.toString()
            else -> ""
        }

        val normalized = rawPath.replace("\\", "/").trim('/')
        if (normalized.isBlank()) {
            return listOf(song.title)
        }

        val segments = normalized.split("/").filter { it.isNotBlank() }
        val cleanSegments = segments.map { segment -> segment.substringAfter(':', segment) }

        val rootIndex = cleanSegments.indexOf(rootName)
        return if (rootIndex >= 0 && rootIndex + 1 < cleanSegments.size) {
            cleanSegments.drop(rootIndex + 1)
        } else {
            cleanSegments
        }
    }

    private fun startsWith(full: List<String>, prefix: List<String>): Boolean {
        if (prefix.isEmpty()) return true
        if (prefix.size > full.size) return false

        for (i in prefix.indices) {
            if (full[i] != prefix[i]) {
                return false
            }
        }
        return true
    }

    fun getAllScannedSongs(): List<MusicMetadata> {
        return scannedFilesManager.scannedSongs.value
    }
}
