/**
* MusicScanner.kt - 音乐文件扫描器
*
* 使用并行扫描来提高扫描速度，支持提取元数据
* 支持解析m3u8播放列表文件（包括相对路径）
*
* 2026 现代化更新：使用 MediaMetadataRetriever 提取音频元数据（替代 jaudiotagger）
*/
package org.bibichan.union.player.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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

            // 创建通道用于并行处理
            val fileChannel = Channel<File>(capacity = Channel.UNLIMITED)

            // 启动多个消费者协程并行处理文件
            val consumers = List(4) { consumerId ->
                launch {
                    for (file in fileChannel) {
                        try {
                            val metadata = processAudioFile(file)
                            if (metadata != null) {
                                songs[metadata] = metadata
                                Log.d(TAG, "Consumer $consumerId: Processed ${file.name}")
                            }

                            // 更新进度
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

            // 扫描目录并发送文件到通道
            val scanJob = launch {
                scanDirectoryRecursive(directory, fileChannel, recursive, scannedDirs)
                fileChannel.close()
            }

            // 等待所有文件处理完成
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
                        // 音频文件
                        AudioFormat.supportedExtensions.contains(extension) -> {
                            channel.send(file)
                        }
                        // 播放列表文件在后面处理
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
    * 
    * 使用 MediaMetadataRetriever 提取音频元数据（替代 jaudiotagger）
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

            // 提取元数据
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

            // 提取时長（毫秒）
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L

            // 提取專輯封面
            val albumArt = extractAlbumArt(retriever)

            retriever.release()

            MusicMetadata(
                id = System.nanoTime(),
                title = title,
                artist = artist,
                album = album,
                duration = duration,
                filePath = file.absolutePath,
                uri = Uri.fromFile(file),
                albumArt = albumArt,
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
    * 
    * 使用 MediaMetadataRetriever.embeddedPicture 提取嵌入式封面图片
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
    *
    * 正确处理相对路径：
    * 如果m3u8文件位于 /sdcard/play.m3u8，包含行 "good/good.m4a"
    * 则实际文件路径为 /sdcard/good/good.m4a
    *
    * @param playlistFile 播放列表文件
    * @param musicLibrary 音乐库（用于查找已有的歌曲元数据）
    * @return 解析后的播放列表
    */
    suspend fun parsePlaylistFile(
        playlistFile: File,
        musicLibrary: List<MusicMetadata> = emptyList()
    ): Playlist? = withContext(Dispatchers.IO) {
        try {
            val playlistName = playlistFile.nameWithoutExtension
            val playlistDir = playlistFile.parentFile // 播放列表文件所在目录
            val songs = mutableListOf<MusicMetadata>()

            Log.i(TAG, "Parsing playlist: ${playlistFile.absolutePath}")
            Log.i(TAG, "Playlist directory: ${playlistDir?.absolutePath}")

            playlistFile.readLines().forEach { line ->
                val trimmedLine = line.trim()

                // 跳过注释和空行
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    return@forEach
                }

                Log.d(TAG, "Processing playlist entry: $trimmedLine")

                // 解析文件路径（可能是相对路径或绝对路径）
                val songFile = resolvePlaylistEntry(trimmedLine, playlistDir)
                if (songFile != null && songFile.exists()) {
                    Log.d(TAG, "Resolved to: ${songFile.absolutePath}")

                    // 在音乐库中查找对应的歌曲
                    val song = musicLibrary.find { it.filePath == songFile.absolutePath }
                    if (song != null) {
                        songs.add(song)
                    } else {
                        // 如果歌曲不在库中，创建新的元数据
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
    *
    * 处理相对路径：
    * - 相对路径：相对于播放列表文件所在目录解析
    * - 绝对路径：直接使用
    *
    * @param entry 播放列表中的条目（可能是相对或绝对路径）
    * @param playlistDir 播放列表文件所在目录
    * @return 解析后的文件，如果无法解析则返回null
    */
    private fun resolvePlaylistEntry(entry: String, playlistDir: File?): File? {
        // 移除可能的file://前缀
        val cleanEntry = entry.removePrefix("file://")

        val file = File(cleanEntry)

        return when {
            // 绝对路径
            file.isAbsolute -> {
                if (file.exists()) file else null
            }
            // 相对路径：相对于播放列表目录
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
    *
    * @param context 上下文
    * @param uri 播放列表文件URI
    * @param musicLibrary 音乐库
    * @return 解析后的播放列表
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

            // 从URI获取输入流
            LogManager.d(TAG, "Opening input stream from URI...")
            val inputStream = context.contentResolver.openInputStream(uri)

            if (inputStream == null) {
                LogManager.e(TAG, "Failed to open input stream - contentResolver returned null")
                return@withContext null
            }

            LogManager.d(TAG, "Input stream opened successfully")

            // 获取播放列表文件名
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

            // 读取播放列表内容
            LogManager.d(TAG, "Reading playlist content...")
            val lines = inputStream.bufferedReader().readLines()
            LogManager.i(TAG, "Playlist has ${lines.size} lines")

            inputStream.close()

            val songs = mutableListOf<MusicMetadata>()

            // 尝试获取播放列表文件的实际路径（如果可能）
            val playlistPath = getRealPathFromUri(uri)
            val playlistDir = playlistPath?.let { File(it).parentFile }

            LogManager.i(TAG, "Playlist path: $playlistPath")
            LogManager.i(TAG, "Playlist directory: ${playlistDir?.absolutePath ?: "unknown"}")

            lines.forEach { line ->
                val trimmedLine = line.trim()

                // 跳过注释和空行
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    return@forEach
                }

                LogManager.d(TAG, "Processing entry: $trimmedLine")

                // 尝试解析文件路径
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
                    // 尝试从MediaStore获取路径
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
}
