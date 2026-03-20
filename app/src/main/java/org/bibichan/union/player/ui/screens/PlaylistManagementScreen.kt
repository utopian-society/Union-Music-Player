/**
 * PlaylistManagementScreen.kt - 播放列表管理界面
 *
 * 显示用户导入的播放列表，支持手动导入
 * 每个播放列表显示第一首歌的专辑封面
 */
package org.bibichan.union.player.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.data.Playlist
import org.bibichan.union.player.data.MusicScanner
import org.bibichan.union.player.ui.components.LogManager
import java.util.Locale

private const val TAG = "PlaylistMgmt"

/**
 * 播放列表管理界面
 *
 * @param playlists 当前导入的播放列表
 * @param onImportPlaylist 导入播放列表的回调
 * @param onPlaylistClick 点击播放列表的回调
 * @param onPlaylistDelete 删除播放列表的回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistManagementScreen(
    playlists: List<Playlist>,
    onImportPlaylist: (Uri) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onPlaylistDelete: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showImportDialog by remember { mutableStateOf(false) }

    LogManager.d(TAG, "Rendering PlaylistManagementScreen with ${playlists.size} playlists")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Playlists",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    // 导入按钮
                    IconButton(onClick = { 
                        LogManager.i(TAG, "Import button clicked")
                        showImportDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Import Playlist")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (playlists.isEmpty()) {
                // 空状态 - 提示用户导入播放列表
                EmptyPlaylistContent(
                    onImportClick = { 
                        LogManager.i(TAG, "Empty state import button clicked")
                        showImportDialog = true 
                    }
                )
            } else {
                // 显示播放列表网格
                PlaylistGrid(
                    playlists = playlists,
                    onPlaylistClick = onPlaylistClick,
                    onPlaylistDelete = onPlaylistDelete
                )
            }
        }
    }

    // 导入对话框
    if (showImportDialog) {
        PlaylistImportDialog(
            onDismiss = { 
                LogManager.d(TAG, "Import dialog dismissed")
                showImportDialog = false 
            },
            onFileSelected = { uri ->
                LogManager.i(TAG, "=== FILE SELECTED FROM DIALOG ===")
                LogManager.i(TAG, "Selected URI: $uri")
                showImportDialog = false
                onImportPlaylist(uri)
            }
        )
    }
}

/**
 * 空播放列表状态
 */
@Composable
private fun EmptyPlaylistContent(
    onImportClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Playlists",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Import M3U or M3U8 playlist files",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            FilledTonalButton(
                onClick = onImportClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
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

/**
 * 播放列表网格
 */
@Composable
private fun PlaylistGrid(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onPlaylistDelete: (Playlist) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 8.dp,
            bottom = 80.dp // 为底部播放器预留空间
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = playlists,
            key = { it.id }
        ) { playlist ->
            PlaylistCard(
                playlist = playlist,
                onClick = { onPlaylistClick(playlist) },
                onDelete = { onPlaylistDelete(playlist) }
            )
        }
    }
}

/**
 * 播放列表卡片
 *
 * 显示播放列表名称、歌曲数量和第一首歌的专辑封面
 */
@Composable
private fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // 播放列表封面（使用第一首歌的专辑封面）
            PlaylistArtwork(
                playlist = playlist,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 播放列表信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${playlist.songs.size} songs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 更多选项按钮
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 播放列表封面组件
 *
 * 显示第一首歌的专辑封面，如果没有则显示占位符
 */
@Composable
private fun PlaylistArtwork(
    playlist: Playlist,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 获取第一首歌的专辑封面
    val firstSong = playlist.songs.firstOrNull()
    val artworkUri = firstSong?.let { song ->
        // 优先使用albumArtPath，其次使用uri
        song.albumArtPath?.let { Uri.parse(it) }
            ?: song.uri
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (artworkUri != null && artworkUri != Uri.EMPTY) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(artworkUri)
                    .crossfade(true)
                    .build(),
                contentDescription = "Playlist artwork for ${playlist.name}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // 无封面时显示占位符
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "No playlist artwork",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

/**
 * 播放列表导入对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistImportDialog(
    onDismiss: () -> Unit,
    onFileSelected: (Uri) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    LogManager.d(TAG, "Showing PlaylistImportDialog")

    // 文件选择启动器
    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        LogManager.i(TAG, "=== FILE PICKER RETURNED ===")
        if (uri != null) {
            LogManager.i(TAG, "File picked: $uri")
            LogManager.i(TAG, "URI scheme: ${uri.scheme}")
            LogManager.i(TAG, "URI authority: ${uri.authority}")
            LogManager.i(TAG, "URI path: ${uri.path}")
            LogManager.i(TAG, "URI encoded path: ${uri.encodedPath}")
            
            onFileSelected(uri)
        } else {
            LogManager.w(TAG, "File picker returned null URI - user likely cancelled")
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import Playlist",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select an M3U or M3U8 playlist file to import",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Supported formats: .m3u, .m3u8",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    LogManager.i(TAG, "=== BROWSE FILES BUTTON CLICKED ===")
                    LogManager.i(TAG, "Launching file picker with MIME type: */*")
                    
                    // Try different MIME types
                    try {
                        filePickerLauncher.launch("*/*")
                        LogManager.d(TAG, "File picker launched successfully")
                    } catch (e: Exception) {
                        LogManager.e(TAG, "Failed to launch file picker: ${e.message}", e)
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Browse Files")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * 格式化时长
 */
fun formatPlaylistDuration(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}
