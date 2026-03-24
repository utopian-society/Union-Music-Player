/**
 * MusicScanner.kt - 音乐文件扫描器
 *
 * 使用并行扫描来提高扫描速度，支持提取元数据
 * 支持解析m3u8播放列表文件（包括相对路径）
 *
 * 2026 现代化更新：使用 MediaMetadataRetriever 提取音频元数据（替代 jaudiotagger）
 * 2026-03-24: 提取封面并缓存到本地文件，支援 Library 顯示與重啟持久化
 */
package org.bibichan.union.player.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.bibichan.union.player.ui.components.LogManager

/**
 * 音乐文件扫描器
 *
 * 使用协程并行扫描指定目录，提取音乐文件元数据
 */
class MusicScanner(private val context: Context) {
    private val TAG = "MusicScanner"

    // 扫描状态
    private val scanningJobs = ConcurrentHashMap<String, Job>()
    private val isScanning = AtomicInteger(0)

    /**
     * 扫描状态流
     */
    sealed class ScanState {
        object Idle : ScanState()
        data class Scanning(val progress: Float, val currentFile: String) : ScanState()
        data class Completed(val result: ScanResult) : ScanState()
        data class Error(val message: String) : ScanState()
    }

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    /**
     * 扫描指定目录
     *
     * @param directoryPath 目录路径
     * @param recursive 是否递归扫描子目录
     * @return 扫描结果
     */
    suspend fun scanDirectory(
        directoryPath: String,
        recursive: Boolean = true
    ): ScanResult = withContext(Dispatchers.IO) {
        if (isScanning.get() > 0) {
            Log.w(TAG, "Already scanning, skipping new scan request")
            return@withContext ScanResult()
        }

        isScanning.set(1)
        val startTime = System.currentTimeMillis()

        try {
            _scanState.value = ScanState.Scanning(0f, "Starting scan...")

            val directory = File(directoryPath)
            if (!directory.exists() || !directory.isDirectory) {
                _scanState.value = ScanState.Error("Invalid directory: $directoryPath")
                return@withContext ScanResult()
            }

            val songs = ConcurrentHashMap<MusicMetadata, MusicMetadata>()
            val playlists = ConcurrentHashMap<String, Playlist>()
            val scannedFiles = AtomicInteger(0)
            val scannedDirs = AtomicInteger(0)

            val fileChannel = Channel<File>(capacity = Channel.UNLIMITED)

            val consumers = List(4) { consumerId ->
                launch {
                    for (file in fileChannel) {
                        try {
                            val metadata = processAudioFile(file)
                            if (metadata != null) {
                                songs[metadata] = metadata
                                Log.d(TAG, "Consumer $consumerId: Processed ${file.name}")
                            }

                            val processed = scannedFiles.incrementAndGet()
                            _scanState.value = ScanState.Scanning(
                                progress = processed.toFloat() / 100,
                                currentFile = file.name
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Consumer $consumerId error processing ${file.name}", e)
                        }
                    }
                }
            }

            val scanJob = launch {
                scanDirectoryRecursive(directory, fileChannel, recursive, scannedDirs)
                fileChannel.close()
            }

            scanJob.join()
            consumers.forEach { it.join() }

            val scanTime = System.currentTimeMillis() - startTime
            val result = ScanResult(
                songs = songs.values.toList().sortedBy { it.title },
                playlists = playlists.values.toList(),
                scanTime = scanTime,
                scannedDirectories = scannedDirs.get(),
                scannedFiles = scannedFiles.get()
            )

            _scanState.value = ScanState.Completed(result)
            Log.i(TAG, "Scan completed: ${result.songs.size} songs, ${result.playlists.size} playlists, ${scanTime}ms")

            result
        } catch (e: Exception) {
            Log.e(TAG, "Scan error", e)
            _scanState.value = ScanState.Error(e.message ?: "Unknown error")
            ScanResult()
        } finally {
            isScanning.set(0)
        }
    }

    /**
     * 递归扫描目录
     */
    private suspend fun scanDirectoryRecursive(
        directory: File,
        channel: Channel<File>,
        recursive: Boolean,
        dirCounter: AtomicInteger
    ) {
        dirCounter.incrementAndGet()
        directory.listFiles()?.forEach { file ->
            when {
                file.isDirectory && recursive -> {
                    scanDirectoryRecursive(file, channel, recursive, dirCounter)
                }
                file.isFile -> {
                    val extension = file.extension.lowercase()
                    when {
                        AudioFormat.supportedExtensions.contains(extension) -> {
                            channel.send(file)
                        }
                        extension == "m3u" || extension == "m3u8" -> {
                            // 播放列表单独处理
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理单个音频文件，提取元数据
     */
    private fun processAudioFile(file: File): MusicMetadata? {
        return try {
            val extension = file.extension.lowercase()
            val format = AudioFormat.fromExtension(extension)

            if (format == AudioFormat.UNKNOWN) {
                return null
            }

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.takeIf { it.isNotBlank() } ?: "Unknown Album"
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                ?.takeIf { it.isNotBlank() }
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
            val trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull()

            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

            val savedAlbumArt = retriever.embeddedPicture?.let { bytes ->
                val key = AlbumArtCache.keyFrom(file.absolutePath)
                AlbumArtCache.saveEmbeddedPicture(context, key, bytes)
            }

            retriever.release()

            MusicMetadata(
                id = System.nanoTime(),
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                filePath = file.absolutePath,
                uri = Uri.fromFile(file),
                albumArt = savedAlbumArt?.bitmap,
                albumArtPath = savedAlbumArt?.uriString,
                genre = genre,
                year = year,
                trackNumber = trackNumber,
                format = format
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing file: ${file.name}", e)
            null
        }
    }

    /**
     * 创建基本元数据（当无法读取标签时）
     */
    private fun createBasicMetadata(file: File, format: AudioFormat): MusicMetadata {
        return MusicMetadata(
            id = System.nanoTime(),
            title = file.nameWithoutExtension,
            artist = "Unknown Artist",
            album = "Unknown Album",
            duration = 0,
            filePath = file.absolutePath,
            uri = Uri.fromFile(file),
            format = format
        )
    }

    /**
     * 从 MediaMetadataRetriever 中提取专辑封面
     */
    private fun extractAlbumArt(retriever: MediaMetadataRetriever): Bitmap? {
        return try {
            val imageData = retriever.embeddedPicture
            if (imageData != null) {
                BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting album art", e)
            null
        }
    }

    /**
     * 解析M3U/M3U8播放列表文件
     */
    suspend fun parsePlaylistFile(
        playlistFile: File,
        musicLibrary: List<MusicMetadata> = emptyList()
    ): Playlist? = withContext(Dispatchers.IO) {
        try {
            val playlistName = playlistFile.nameWithoutExtension
            val playlistDir = playlistFile.parentFile
            val songs = mutableListOf<MusicMetadata>()

            Log.i(TAG, "Parsing playlist: ${playlistFile.absolutePath}")
            Log.i(TAG, "Playlist directory: ${playlistDir?.absolutePath}")

            playlistFile.readLines().forEach { line ->
                val trimmedLine = line.trim()

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    return@forEach
                }

                Log.d(TAG, "Processing playlist entry: $trimmedLine")

                val songFile = resolvePlaylistEntry(trimmedLine, playlistDir)
                if (songFile != null && songFile.exists()) {
                    Log.d(TAG, "Resolved to: ${songFile.absolutePath}")

                    val song = musicLibrary.find { it.filePath == songFile.absolutePath }
                    if (song != null) {
                        songs.add(song)
                    } else {
                        val metadata = processAudioFile(songFile)
                        if (metadata != null) {
                            songs.add(metadata)
                        }
                    }
                } else {
                    Log.w(TAG, "Could not resolve path: $trimmedLine")
                }
            }

            Log.i(TAG, "Playlist '$playlistName' contains ${songs.size} songs")

            Playlist(
                id = System.nanoTime(),
                name = playlistName,
                songs = songs,
                filePath = playlistFile.absolutePath
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing playlist: ${playlistFile.name}", e)
            null
        }
    }

    /**
     * 解析播放列表条目路径
     */
    private fun resolvePlaylistEntry(entry: String, playlistDir: File?): File? {
        val cleanEntry = entry.removePrefix("file://")

        val file = File(cleanEntry)

        return when {
            file.isAbsolute -> {
                if (file.exists()) file else null
            }
            playlistDir != null -> {
                val relativeFile = File(playlistDir, cleanEntry)
                Log.d(TAG, "Trying relative path: ${relativeFile.absolutePath}")
                if (relativeFile.exists()) relativeFile else null
            }
            else -> null
        }
    }

    /**
     * 从URI解析播放列表（用于从文件选择器选择的播放列表）
     */
    suspend fun parsePlaylistFromUri(
        uri: Uri,
        musicLibrary: List<MusicMetadata> = emptyList()
    ): Playlist? = withContext(Dispatchers.IO) {
        try {
            LogManager.i(TAG, "=== parsePlaylistFromUri START ===")
            LogManager.i(TAG, "URI: $uri")
            LogManager.i(TAG, "URI scheme: ${uri.scheme}")
            LogManager.i(TAG, "URI authority: ${uri.authority}")
            LogManager.i(TAG, "URI path: ${uri.path}")

            LogManager.d(TAG, "Opening input stream from URI...")
            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                LogManager.e(TAG, "Failed to open input stream - contentResolver returned null")
                return@withContext null
            }

            LogManager.d(TAG, "Input stream opened successfully")

            LogManager.d(TAG, "Querying content resolver for file name...")
            val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex("_display_name")
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    val name = cursor.getString(nameIndex)
                    LogManager.d(TAG, "File name from cursor: $name")
                    name
                } else {
                    LogManager.w(TAG, "Could not get file name from cursor, using default")
                    "Unknown Playlist"
                }
            } ?: "Unknown Playlist"

            val playlistName = File(fileName).nameWithoutExtension
            LogManager.i(TAG, "Playlist name: $playlistName")

            LogManager.d(TAG, "Reading playlist content...")
            val lines = inputStream.bufferedReader().readLines()
            LogManager.i(TAG, "Playlist has ${lines.size} lines")

            inputStream.close()

            val songs = mutableListOf<MusicMetadata>()

            val playlistPath = getRealPathFromUri(uri)
            val playlistDir = playlistPath?.let { File(it).parentFile }

            LogManager.i(TAG, "Playlist path: $playlistPath")
            LogManager.i(TAG, "Playlist directory: ${playlistDir?.absolutePath ?: "unknown"}")

            lines.forEach { line ->
                val trimmedLine = line.trim()

                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    return@forEach
                }

                LogManager.d(TAG, "Processing entry: $trimmedLine")

                val songFile = resolvePlaylistEntry(trimmedLine, playlistDir)
                if (songFile != null && songFile.exists()) {
                    LogManager.d(TAG, "Resolved to: ${songFile.absolutePath}")

                    val song = musicLibrary.find { it.filePath == songFile.absolutePath }
                    if (song != null) {
                        songs.add(song)
                        LogManager.d(TAG, "Found song in music library")
                    } else {
                        val metadata = processAudioFile(songFile)
                        if (metadata != null) {
                            songs.add(metadata)
                            LogManager.d(TAG, "Processed audio file")
                        } else {
                            LogManager.w(TAG, "Failed to process audio file: ${songFile.absolutePath}")
                        }
                    }
                } else {
                    LogManager.w(TAG, "Could not resolve path: $trimmedLine")
                }
            }

            LogManager.i(TAG, "=== PARSING COMPLETE ===")
            LogManager.i(TAG, "Playlist '$playlistName' contains ${songs.size} songs")

            val playlist = Playlist(
                id = System.nanoTime(),
                name = playlistName,
                songs = songs,
                filePath = playlistPath
            )

            LogManager.i(TAG, "Created playlist object with ID: ${playlist.id}")

            playlist
        } catch (e: Exception) {
            LogManager.e(TAG, "Error parsing playlist from URI: ${e.message}", e)
            null
        }
    }

    /**
     * 尝试从URI获取真实文件路径
     */
    private fun getRealPathFromUri(uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "file" -> uri.path
                "content" -> {
                    context.contentResolver.query(uri, arrayOf("_data"), null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            val dataIndex = cursor.getColumnIndex("_data")
                            if (dataIndex >= 0) cursor.getString(dataIndex) else null
                        } else null
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not get real path from URI", e)
            null
        }
    }

    /**
     * 停止扫描
     */
    fun stopScan() {
        scanningJobs.values.forEach { it.cancel() }
        scanningJobs.clear()
        isScanning.set(0)
        _scanState.value = ScanState.Idle
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        stopScan()
    }

    /**
     * 掃描 SAF DocumentFile 資料夾
     */
    suspend fun scanDocumentFolder(
        folderUri: Uri,
        context: Context
    ): ScanResult = withContext(Dispatchers.IO) {
        if (isScanning.get() > 0) {
            Log.w(TAG, "Already scanning, skipping new scan request")
            return@withContext ScanResult()
        }

        isScanning.set(1)
        val startTime = System.currentTimeMillis()

        try {
            _scanState.value = ScanState.Scanning(0f, "Starting scan...")

            val documentFile = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, folderUri)
            if (documentFile == null || !documentFile.exists()) {
                _scanState.value = ScanState.Error("Invalid folder URI: $folderUri")
                return@withContext ScanResult()
            }

            Log.i(TAG, "First pass: counting audio files...")
            _scanState.value = ScanState.Scanning(0f, "Counting files...")
            val totalFiles = AtomicInteger(0)
            countAudioFilesRecursive(documentFile, totalFiles)
            val total = totalFiles.get()
            Log.i(TAG, "Found $total audio files to process")

            if (total == 0) {
                _scanState.value = ScanState.Error("No audio files found in selected folder")
                return@withContext ScanResult()
            }

            val songs = ConcurrentHashMap<MusicMetadata, MusicMetadata>()
            val scannedFiles = AtomicInteger(0)
            val scannedDirs = AtomicInteger(0)
            val errorCount = AtomicInteger(0)

            val fileChannel = Channel<androidx.documentfile.provider.DocumentFile>(capacity = Channel.UNLIMITED)

            val scanScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

            val consumers = List(2) { consumerId ->
                scanScope.launch {
                    for (file in fileChannel) {
                        if (!isActive) break

                        try {
                            val metadata = processDocumentFile(file, context)
                            if (metadata != null) {
                                songs[metadata] = metadata
                                Log.d(TAG, "Consumer $consumerId: Processed ${file.name}")
                            }

                            val processed = scannedFiles.incrementAndGet()
                            val progress = processed.toFloat() / total
                            _scanState.value = ScanState.Scanning(
                                progress = progress.coerceIn(0f, 1f),
                                currentFile = "$processed/$total: ${file.name ?: "Unknown"}"
                            )

                            kotlinx.coroutines.delay(10)
                        } catch (e: Exception) {
                            Log.e(TAG, "Consumer $consumerId error processing ${file.name}", e)
                            errorCount.incrementAndGet()
                        }
                    }
                }
            }

            val scanJob = scanScope.launch {
                scanDocumentFileRecursive(documentFile, fileChannel, scannedDirs)
                fileChannel.close()
            }

            scanJob.join()
            consumers.forEach { it.join() }

            val scanTime = System.currentTimeMillis() - startTime
            val result = ScanResult(
                songs = songs.values.toList().sortedBy { it.title },
                playlists = emptyList(),
                scanTime = scanTime,
                scannedDirectories = scannedDirs.get(),
                scannedFiles = scannedFiles.get()
            )

            _scanState.value = ScanState.Completed(result)
            Log.i(TAG, "Document scan completed: ${result.songs.size} songs, ${errorCount.get()} errors, ${scanTime}ms")

            result
        } catch (e: Exception) {
            Log.e(TAG, "Document scan error", e)
            _scanState.value = ScanState.Error(e.message ?: "Unknown error")
            ScanResult()
        } finally {
            isScanning.set(0)
        }
    }

    /**
     * 遞歸計算音頻文件數量
     */
    private suspend fun countAudioFilesRecursive(
        directory: androidx.documentfile.provider.DocumentFile,
        counter: AtomicInteger
    ) {
        directory.listFiles().forEach { file ->
            when {
                file.isDirectory -> {
                    countAudioFilesRecursive(file, counter)
                }
                file.isFile -> {
                    val name = file.name ?: ""
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (AudioFormat.supportedExtensions.contains(extension)) {
                        counter.incrementAndGet()
                    }
                }
            }
        }
    }

    /**
     * 遞歸掃描 DocumentFile 目錄
     */
    private suspend fun scanDocumentFileRecursive(
        directory: androidx.documentfile.provider.DocumentFile,
        channel: Channel<androidx.documentfile.provider.DocumentFile>,
        dirCounter: AtomicInteger
    ) {
        dirCounter.incrementAndGet()
        directory.listFiles().forEach { file ->
            when {
                file.isDirectory -> {
                    scanDocumentFileRecursive(file, channel, dirCounter)
                }
                file.isFile -> {
                    val name = file.name ?: ""
                    val extension = name.substringAfterLast('.', "").lowercase()
                    if (AudioFormat.supportedExtensions.contains(extension)) {
                        channel.send(file)
                    }
                }
            }
        }
    }

    /**
     * 處理單個 DocumentFile 音頻文件
     */
    private fun processDocumentFile(
        documentFile: androidx.documentfile.provider.DocumentFile,
        context: Context
    ): MusicMetadata? {
        val name = documentFile.name ?: return null
        val extension = name.substringAfterLast('.', "").lowercase()
        val format = AudioFormat.fromExtension(extension)

        if (format == AudioFormat.UNKNOWN) {
            return null
        }

        val retriever = MediaMetadataRetriever()

        return try {
            retriever.setDataSource(context, documentFile.uri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?.takeIf { it.isNotBlank() } ?: name.substringBeforeLast('.')
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?.takeIf { it.isNotBlank() } ?: "Unknown Artist"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?.takeIf { it.isNotBlank() } ?: "Unknown Album"
            val genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                ?.takeIf { it.isNotBlank() }
            val year = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)?.toIntOrNull()
            val trackNumber = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)?.toIntOrNull()
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

            val savedAlbumArt = retriever.embeddedPicture?.let { bytes ->
                val key = AlbumArtCache.keyFrom(documentFile.uri.toString())
                AlbumArtCache.saveEmbeddedPicture(context, key, bytes)
            }

            val filePath = getRealPathFromUri(documentFile.uri) ?: documentFile.uri.toString()

            MusicMetadata(
                id = System.nanoTime(),
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                filePath = filePath,
                uri = documentFile.uri,
                albumArt = savedAlbumArt?.bitmap,
                albumArtPath = savedAlbumArt?.uriString,
                genre = genre,
                year = year,
                trackNumber = trackNumber,
                format = format
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error processing DocumentFile: ${documentFile.name}", e)
            null
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error releasing MediaMetadataRetriever", e)
            }
        }
    }
}
