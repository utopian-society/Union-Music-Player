/**
 * ScannedFilesManager.kt - 已掃描檔案管理器
 *
 * 管理用戶選擇掃描的資料夾，提供持久化存儲和狀態管理。
 * 使用 SharedPreferences 存儲已掃描的資料夾 URI 列表與每個資料夾對應的歌曲列表（JSON）。
 *
 * 2026-03-22: 新增功能，支援用戶選擇資料夾掃描
 * 2026-03-24: 增強持久化：保存 songsByFolder，支援重啟後恢復 Library
 */
package org.bibichan.union.player.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "ScannedFilesManager"

/**
 * 掃描資料夾資料
 *
 * @param uri 資料夾 URI (來自 SAF)
 * @param name 顯示名稱
 * @param path 資料夾路徑
 * @param scanTime 掃描時間 (毫秒時間戳)
 * @param songCount 掃描到的歌曲數量
 */
data class ScannedFolder(
    val uri: Uri,
    val name: String,
    val path: String,
    val scanTime: Long,
    val songCount: Int
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("uri", uri.toString())
            put("name", name)
            put("path", path)
            put("scanTime", scanTime)
            put("songCount", songCount)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): ScannedFolder {
            return ScannedFolder(
                uri = Uri.parse(json.getString("uri")),
                name = json.getString("name"),
                path = json.getString("path"),
                scanTime = json.getLong("scanTime"),
                songCount = json.getInt("songCount")
            )
        }
    }
}

/**
 * 檔案樹節點 - 用於 Files 頁面的樹狀結構顯示
 */
data class FileTreeNode(
    val name: String,
    val uri: Uri,
    val isDirectory: Boolean,
    val children: List<FileTreeNode> = emptyList(),
    val song: MusicMetadata? = null
)

/**
 * 已掃描檔案管理器
 *
 * 單例模式，管理已掃描的資料夾和音樂檔案。
 * - 使用 SharedPreferences 持久化存儲已掃描的資料夾列表與 songsByFolder
 * - 提供掃描結果的緩存
 * - 支援 Flow 響應式更新
 */
class ScannedFilesManager private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "scanned_files_prefs"
        private const val KEY_SCANNED_FOLDERS = "scanned_folders"
        private const val KEY_SONGS_BY_FOLDER = "songs_by_folder"

        @Volatile
        private var INSTANCE: ScannedFilesManager? = null

        fun getInstance(context: Context): ScannedFilesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ScannedFilesManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // 已掃描的資料夾列表
    private val _scannedFolders = MutableStateFlow<List<ScannedFolder>>(emptyList())
    val scannedFolders: StateFlow<List<ScannedFolder>> = _scannedFolders.asStateFlow()

    // 所有已掃描的歌曲（合併所有資料夾）
    private val _scannedSongs = MutableStateFlow<List<MusicMetadata>>(emptyList())
    val scannedSongs: StateFlow<List<MusicMetadata>> = _scannedSongs.asStateFlow()

    // 按資料夾分組的歌曲
    private val _songsByFolder = MutableStateFlow<Map<Uri, List<MusicMetadata>>>(emptyMap())
    val songsByFolder: StateFlow<Map<Uri, List<MusicMetadata>>> = _songsByFolder.asStateFlow()

    // 是否有掃描的資料夾
    val hasScannedFolders: StateFlow<Boolean> = _scannedFolders
        .map { it.isNotEmpty() }
        .stateIn(
            scope = kotlinx.coroutines.GlobalScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // 掃描狀態
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    // 掃描進度
    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    init {
        // 從 SharedPreferences 載入已保存的資料
        loadFromPreferences()
    }

    /**
     * 以「單一掃描結果」替換整個索引
     *
     * 用於新掃描完成後清空舊索引的需求。
     */
    fun replaceAllWithSingleFolder(folderUri: Uri, name: String, path: String, songs: List<MusicMetadata>) {
        val folder = ScannedFolder(
            uri = folderUri,
            name = name,
            path = path,
            scanTime = System.currentTimeMillis(),
            songCount = songs.size
        )

        _scannedFolders.value = listOf(folder)
        _songsByFolder.value = mapOf(folderUri to songs)
        updateScannedSongs()
        saveToPreferences()

        Log.i(TAG, "Replaced index with folder: $name (${songs.size} songs)")
    }

    /**
     * 添加掃描的資料夾
     *
     * @param folderUri 資料夾 URI (來自 SAF)
     * @param name 顯示名稱
     * @param songs 掃描到的歌曲列表
     */
    fun addScannedFolder(folderUri: Uri, name: String, path: String, songs: List<MusicMetadata>) {
        val folder = ScannedFolder(
            uri = folderUri,
            name = name,
            path = path,
            scanTime = System.currentTimeMillis(),
            songCount = songs.size
        )

        val existingIndex = _scannedFolders.value.indexOfFirst { it.uri == folderUri }

        if (existingIndex >= 0) {
            val updatedFolders = _scannedFolders.value.toMutableList()
            updatedFolders[existingIndex] = folder
            _scannedFolders.value = updatedFolders
        } else {
            _scannedFolders.value = _scannedFolders.value + folder
        }

        // 更新歌曲列表（同 key 會覆蓋舊列表）
        _songsByFolder.value = _songsByFolder.value + (folderUri to songs)
        updateScannedSongs()

        // 保存到 SharedPreferences
        saveToPreferences()

        Log.i(TAG, "Added scanned folder: $name with ${songs.size} songs")
    }

    /**
     * 移除掃描的資料夾
     */
    fun removeScannedFolder(folderUri: Uri) {
        val folder = _scannedFolders.value.find { it.uri == folderUri }
        if (folder != null) {
            _scannedFolders.value = _scannedFolders.value - folder
            _songsByFolder.value = _songsByFolder.value - folderUri
            updateScannedSongs()
            saveToPreferences()
            Log.i(TAG, "Removed scanned folder: ${folder.name}")
        }
    }

    /**
     * 獲取指定資料夾的歌曲
     */
    fun getSongsInFolder(folderUri: Uri): List<MusicMetadata> {
        return _songsByFolder.value[folderUri] ?: emptyList()
    }

    /**
     * 更新所有歌曲列表（合併所有資料夾的歌曲）
     */
    private fun updateScannedSongs() {
        val allSongs = _songsByFolder.value.values
            .flatten()
            .distinctBy { song ->
                val uriKey = song.uri.toString()
                if (uriKey.isNotBlank() && uriKey != Uri.EMPTY.toString()) uriKey else song.filePath
            }

        _scannedSongs.value = allSongs
        Log.d(TAG, "Total scanned songs: ${allSongs.size}")
    }

    /**
     * 刷新所有資料夾（重新掃描）
     * 這個方法需要 MusicScanner 來執行實際掃描
     */
    suspend fun refreshAllFolders(scanner: MusicScanner, context: Context) {
        _isScanning.value = true
        try {
            val folders = _scannedFolders.value.toList()
            var completed = 0

            for (folder in folders) {
                _scanProgress.value = if (folders.isEmpty()) 0f else completed.toFloat() / folders.size

                val result = scanner.scanDocumentFolder(folder.uri, context)
                if (result.songs.isNotEmpty()) {
                    addScannedFolder(folder.uri, folder.name, folder.path, result.songs)
                }

                completed++
            }

            _scanProgress.value = 1f
        } finally {
            _isScanning.value = false
        }
    }

    /**
     * 從 SharedPreferences 載入資料
     */
    private fun loadFromPreferences() {
        try {
            // 載入資料夾列表
            val foldersJson = prefs.getString(KEY_SCANNED_FOLDERS, null)
            if (foldersJson != null) {
                val jsonArray = JSONArray(foldersJson)
                val folders = mutableListOf<ScannedFolder>()
                for (i in 0 until jsonArray.length()) {
                    folders.add(ScannedFolder.fromJson(jsonArray.getJSONObject(i)))
                }
                _scannedFolders.value = folders
                Log.i(TAG, "Loaded ${folders.size} scanned folders from preferences")
            }

            // 載入 songsByFolder
            val songsByFolderJson = prefs.getString(KEY_SONGS_BY_FOLDER, null)
            if (songsByFolderJson != null) {
                val root = JSONObject(songsByFolderJson)
                val map = mutableMapOf<Uri, List<MusicMetadata>>()

                val keys = root.keys()
                while (keys.hasNext()) {
                    val folderUriString = keys.next()
                    val folderUri = Uri.parse(folderUriString)

                    val songsArray = root.optJSONArray(folderUriString) ?: JSONArray()
                    val songs = mutableListOf<MusicMetadata>()
                    for (i in 0 until songsArray.length()) {
                        val songJson = songsArray.getJSONObject(i)
                        songs.add(MusicMetadata.fromJson(songJson))
                    }

                    map[folderUri] = songs
                }

                _songsByFolder.value = map
                updateScannedSongs()

                Log.i(TAG, "Loaded songsByFolder from preferences: ${map.size} folders")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading from preferences", e)
        }
    }

    /**
     * 保存到 SharedPreferences
     */
    private fun saveToPreferences() {
        try {
            val editor = prefs.edit()

            // 保存資料夾列表
            val foldersJson = JSONArray()
            _scannedFolders.value.forEach { folder ->
                foldersJson.put(folder.toJson())
            }
            editor.putString(KEY_SCANNED_FOLDERS, foldersJson.toString())

            // 保存 songsByFolder（不包含 Bitmap）
            val songsRoot = JSONObject()
            _songsByFolder.value.forEach { (folderUri, songs) ->
                val songsJson = JSONArray()
                songs.forEach { song ->
                    songsJson.put(song.toJson())
                }
                songsRoot.put(folderUri.toString(), songsJson)
            }
            editor.putString(KEY_SONGS_BY_FOLDER, songsRoot.toString())

            editor.apply()

            Log.d(TAG, "Saved ${_scannedFolders.value.size} folders and ${_songsByFolder.value.size} song groups")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving to preferences", e)
        }
    }

    /**
     * 清除所有掃描資料
     */
    fun clearAll() {
        _scannedFolders.value = emptyList()
        _scannedSongs.value = emptyList()
        _songsByFolder.value = emptyMap()
        prefs.edit().clear().apply()
        Log.i(TAG, "Cleared all scanned data")
    }

    /**
     * 獲取資料夾的樹狀結構（舊實作：基於 filePath 分割，對 SAF content URI 不一定可靠）
     */
    fun getFileTreeForFolder(folderUri: Uri): List<FileTreeNode> {
        val songs = _songsByFolder.value[folderUri] ?: return emptyList()

        val rootNodes = mutableListOf<FileTreeNode>()
        val pathMap = mutableMapOf<String, MutableList<FileTreeNode>>()

        val sortedSongs = songs.sortedBy { it.filePath }

        for (song in sortedSongs) {
            val pathParts = song.filePath.split("/")
            var currentPath = ""

            for (i in 0 until pathParts.size - 1) {
                val dirName = pathParts[i]
                val dirPath = if (currentPath.isEmpty()) dirName else "$currentPath/$dirName"

                if (!pathMap.containsKey(dirPath)) {
                    val node = FileTreeNode(
                        name = dirName,
                        uri = Uri.parse("folder://$dirPath"),
                        isDirectory = true,
                        children = emptyList()
                    )

                    if (currentPath.isEmpty()) {
                        rootNodes.add(node)
                    } else {
                        pathMap[currentPath]?.add(node)
                    }
                    pathMap[dirPath] = mutableListOf()
                }

                currentPath = dirPath
            }

            val songNode = FileTreeNode(
                name = song.title,
                uri = Uri.parse(song.filePath),
                isDirectory = false,
                song = song
            )

            val parentPath = pathParts.dropLast(1).joinToString("/")
            if (parentPath.isEmpty()) {
                rootNodes.add(songNode)
            } else {
                pathMap[parentPath]?.add(songNode)
            }
        }

        return rootNodes
    }
}
