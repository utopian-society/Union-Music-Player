/**
 * MusicScanner.kt - 音乐文件扫描器
 *
 * 使用并行扫描来提高扫描速度，支持提取元数据
 */
package org.bibichan.union.player.data

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

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
            
            val songs = ConcurrentHashMap<MusicMetadata>()
            val playlists = ConcurrentHashMap<Playlist>()
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
                                progress = processed.toFloat() / 100, // 估计总数
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
                        // 播放列表文件
                        extension == "m3u" || extension == "m3u8" -> {
                            // 播放列表在后面处理
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
            
            // 使用jaudiotagger提取元数据
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag ?: return createBasicMetadata(file, format)
            
            // 提取专辑封面
            val albumArt = extractAlbumArt(tag)
            
            // 提取元数据
            val title = tag.getFirst(FieldKey.TITLE).takeIf { it.isNotBlank() } ?: file.nameWithoutExtension
            val artist = tag.getFirst(FieldKey.ARTIST).takeIf { it.isNotBlank() } ?: "Unknown Artist"
            val album = tag.getFirst(FieldKey.ALBUM).takeIf { it.isNotBlank() } ?: "Unknown Album"
            val genre = tag.getFirst(FieldKey.GENRE).takeIf { it.isNotBlank() }
            val year = tag.getFirst(FieldKey.YEAR).toIntOrNull()
            val trackNumber = tag.getFirst(FieldKey.TRACK).toIntOrNull()
            
            // 获取音频时长
            val duration = try {
                (audioFile.audioHeader?.trackLength ?: 0) * 1000L // 转换为毫秒
            } catch (e: Exception) {
                0L
            }
            
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
     * 从标签中提取专辑封面
     */
    private fun extractAlbumArt(tag: org.jaudiotagger.tag.Tag): android.graphics.Bitmap? {
        return try {
            val artworkList = tag.getArtworkList()
            if (artworkList != null && artworkList.size() > 0) {
                val artwork = artworkList[0]
                val imageData = artwork.binaryData
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
        musicLibrary: List<MusicMetadata>
    ): Playlist? = withContext(Dispatchers.IO) {
        try {
            val playlistName = playlistFile.nameWithoutExtension
            val songs = mutableListOf<MusicMetadata>()
            
            playlistFile.readLines().forEach { line ->
                val trimmedLine = line.trim()
                
                // 跳过注释和空行
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                    return@forEach
                }
                
                // 解析文件路径（可能是相对路径或绝对路径）
                val songFile = resolvePlaylistEntry(trimmedLine, playlistFile.parentFile)
                if (songFile != null && songFile.exists()) {
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
                }
            }
            
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
        val file = File(entry)
        
        return when {
            file.isAbsolute -> if (file.exists()) file else null
            playlistDir != null -> {
                val relativeFile = File(playlistDir, entry)
                if (relativeFile.exists()) relativeFile else null
            }
            else -> null
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
