/**
 * PlaylistSelector.kt - 播放列表选择器
 *
 * 实现m3u8播放列表选择功能
 */
package org.bibichan.union.player.ui.components

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistSelector(
    onPlaylistSelected: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var playlistFiles by remember { mutableStateOf<List<PlaylistFile>>(emptyList()) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    
    // 权限检查
    val hasPermission = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    // 文件选择启动器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            onPlaylistSelected(it)
            onDismiss()
        }
    }
    
    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限被授予，扫描播放列表文件
            scanForPlaylistFiles(context) { files ->
                playlistFiles = files
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            scanForPlaylistFiles(context) { files ->
                playlistFiles = files
                isLoading = false
            }
        } else {
            showPermissionDialog = true
            isLoading = false
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Playlist",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            if (showPermissionDialog) {
                // 显示权限请求对话框
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Storage Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To access your playlist files, we need permission to read your storage.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // 显示播放列表文件列表
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (playlistFiles.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QueueMusic,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No playlist files found",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(playlistFiles) { playlistFile ->
                                PlaylistItem(
                                    playlistFile = playlistFile,
                                    onClick = {
                                        onPlaylistSelected(playlistFile.uri)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    // 打开文件选择器
                                    filePickerLauncher.launch("*/*")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Import Playlist")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (showPermissionDialog) {
                TextButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        showPermissionDialog = false
                    }
                ) {
                    Text("Grant Permission")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun PlaylistItem(
    playlistFile: PlaylistFile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = playlistFile.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${playlistFile.type} • ${formatFileSize(playlistFile.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class PlaylistFile(
    val name: String,
    val type: String,
    val size: Long,
    val uri: Uri
)

fun scanForPlaylistFiles(context: Context, callback: (List<PlaylistFile>) -> Unit) {
    // 在后台线程扫描播放列表文件
    kotlinx.coroutines.GlobalScope.launch {
        val playlistFiles = mutableListOf<PlaylistFile>()
        
        // 扫描外部存储中的播放列表文件
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns._ID
        )
        
        val selection = """
            ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.m3u8' OR
            ${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.m3u'
        """.trimIndent()
        
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
        
        val cursor: Cursor? = try {
            context.contentResolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                null,
                sortOrder
            )
        } catch (e: Exception) {
            null
        }
        
        cursor?.use {
            val nameColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            
            while (it.moveToNext()) {
                val name = it.getString(nameColumn)
                val size = it.getLong(sizeColumn)
                val id = it.getLong(idColumn)
                val uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id)
                
                // 获取文件类型
                val type = when {
                    name.endsWith(".m3u8", ignoreCase = true) -> "M3U8"
                    name.endsWith(".m3u", ignoreCase = true) -> "M3U"
                    else -> "PLAYLIST"
                }
                
                playlistFiles.add(PlaylistFile(name.substringBeforeLast("."), type, size, uri))
            }
        }
        
        // 回调主线程
        kotlinx.coroutines.MainScope().launch {
            callback(playlistFiles)
        }
    }
}

fun formatFileSize(bytes: Long): String {
    return when {
        bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
        else -> "$bytes B"
    }
}
