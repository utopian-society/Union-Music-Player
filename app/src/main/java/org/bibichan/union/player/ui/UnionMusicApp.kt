/**
 * UnionMusicApp.kt - 主应用 Composable
 *
 * 这是应用的主 Composable，包含浮动播放器和底部导航栏。
 * 设计参考 Apple Music 风格：Library、Playlist、Files、More 四个主要功能。
 * 使用 Material 3 的 NavigationBar 和 Scaffold 实现标准导航。
 * 
 * 2026-03-22: 新增 Files 標籤頁
 */

package org.bibichan.union.player.ui

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicScanner
import org.bibichan.union.player.data.Playlist
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.ui.components.BottomControlPanel
import org.bibichan.union.player.ui.components.NavItem
import org.bibichan.union.player.ui.components.FloatingPlayer
import org.bibichan.union.player.ui.components.LogManager
import org.bibichan.union.player.ui.library.LibraryScreen
import org.bibichan.union.player.ui.library.LibraryViewModel
import org.bibichan.union.player.ui.library.data.Album
import org.bibichan.union.player.ui.screens.*
import androidx.lifecycle.viewmodel.compose.viewModel

private const val TAG = "UnionMusicApp"

/**
 * UnionMusicApp - 主应用 Composable
 *
 * 使用 Scaffold 作为主布局，包含：
 * - 顶部应用栏（可选）
 * - 主内容区域（基于选中的导航项）
 * - 底部导航栏（绿色 NavigationBar）
 * - 浮动播放器
 *
 * @param musicPlayer 音乐播放器实例
 * @param onRequestPermission 请求权限的回调
 * @param onPermissionResult 权限请求结果回调（用于刷新 UI）
 * @param onFolderPickerRequest 請求打開資料夾選擇器的回調
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnionMusicApp(
    musicPlayer: MusicPlayer,
    onRequestPermission: () -> Unit,
    onPermissionResult: () -> Unit = {},
    onFolderPickerRequest: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 状态管理
    var selectedTab by remember { mutableStateOf(0) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    // 专辑详情导航状态
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }

    // 播放列表状态
    var importedPlaylists by remember { mutableStateOf<List<Playlist>>(emptyList()) }

    // 音乐扫描器
    val musicScanner = remember { MusicScanner(context) }

    // Library ViewModel
    val libraryViewModel: LibraryViewModel = viewModel()
    val albums by libraryViewModel.albums.collectAsState()

    // 定义底部导航项 - 4 個標籤
    val navItems = listOf(
        NavItem(
            route = "library",
            icon = Icons.Default.LibraryMusic,
            label = "Library"
        ),
        NavItem(
            route = "playlist",
            icon = Icons.Default.QueueMusic,
            label = "Playlist"
        ),
        NavItem(
            route = "files",
            icon = Icons.Default.Folder,
            label = "Files"
        ),
        NavItem(
            route = "more",
            icon = Icons.Default.MoreHoriz,
            label = "More"
        )
    )

    // 使用 Scaffold 作为主布局
    Scaffold(
        bottomBar = {
            // 底部导航栏 - 绿色 NavigationBar
            BottomControlPanel(
                items = navItems,
                selectedIndex = selectedTab,
                onItemSelected = { index -> selectedTab = index }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        // 主内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 根据选中的标签和导航状态显示不同的屏幕
            when {
                // 专辑详情界面（优先级最高）
                selectedAlbum != null -> {
                    AlbumDetailScreen(
                        album = selectedAlbum!!,
                        onSongClick = { song, playlist, index ->
                            playSongList(musicPlayer, playlist, index)
                        },
                        onBack = {
                            selectedAlbum = null
                        }
                    )
                }

                // 主标签界面
                else -> when (selectedTab) {
                    0 -> LibraryScreen(
                        musicPlayer = musicPlayer,
                        onRequestPermission = onRequestPermission,
                        onPermissionResult = onPermissionResult
                    )

                    1 -> PlaylistManagementScreen(
                        playlists = importedPlaylists,
                        onImportPlaylist = { uri ->
                            LogManager.i(TAG, "=== PLAYLIST IMPORT STARTED ===")
                            LogManager.i(TAG, "URI: $uri")
                            LogManager.i(TAG, "URI scheme: ${uri.scheme}")
                            LogManager.i(TAG, "URI path: ${uri.path}")
                            scope.launch {
                                importPlaylist(
                                    musicScanner = musicScanner,
                                    uri = uri,
                                    existingPlaylists = importedPlaylists,
                                    onResult = { playlist ->
                                        if (playlist != null) {
                                            importedPlaylists = importedPlaylists + playlist
                                            LogManager.i(TAG, "=== PLAYLIST IMPORT SUCCESS ===")
                                            LogManager.i(TAG, "Playlist: ${playlist.name}")
                                            LogManager.i(TAG, "Songs: ${playlist.songs.size}")
                                            playlist.songs.forEachIndexed { index, song ->
                                                LogManager.d(TAG, " Song $index: ${song.title} - ${song.artist}")
                                            }
                                        } else {
                                            LogManager.e(TAG, "=== PLAYLIST IMPORT FAILED ===")
                                        }
                                    }
                                )
                            }
                        },
                        onPlaylistClick = { playlist ->
                            // 播放播放列表中的歌曲
                            if (playlist.songs.isNotEmpty()) {
                                LogManager.i(TAG, "Playing playlist: ${playlist.name} with ${playlist.songs.size} songs")
                                // TODO: 实现播放列表播放
                            }
                        },
                        onPlaylistDelete = { playlist ->
                            LogManager.i(TAG, "Deleting playlist: ${playlist.name}")
                            importedPlaylists = importedPlaylists.filter { it.id != playlist.id }
                        }
                    )

                    2 -> FilesScreen(
                        musicPlayer = musicPlayer,
                        onFolderPickerRequest = onFolderPickerRequest
                    )

                    3 -> MoreScreen(
                        onRequestPermission = onRequestPermission,
                        onPermissionResult = onPermissionResult,
                        onFolderPickerRequest = onFolderPickerRequest
                    )
                }
            }

            // 浮动播放器 - 当有歌曲播放时显示
            AnimatedVisibility(
                visible = musicPlayer.getCurrentSong() != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FloatingPlayer(
                    musicPlayer = musicPlayer,
                    isVisible = isPlayerExpanded,
                    onExpand = { isPlayerExpanded = true },
                    onCollapse = { isPlayerExpanded = false }
                )
            }
        }
    }
}

/**
 * 播放歌曲列表
 */
private fun playSongList(
    musicPlayer: MusicPlayer,
    songs: List<MusicMetadata>,
    startIndex: Int
) {
    // 设置歌曲列表并开始播放
    musicPlayer.setSongs(songs)
    musicPlayer.play(startIndex)

    LogManager.i(TAG, "Playing song at index $startIndex from list of ${songs.size} songs")

    songs.forEachIndexed { index, song ->
        LogManager.d(TAG, "Song $index: ${song.title} - ${song.artist}")
    }
}

/**
 * 导入播放列表
 */
private suspend fun importPlaylist(
    musicScanner: MusicScanner,
    uri: Uri,
    existingPlaylists: List<Playlist>,
    onResult: (Playlist?) -> Unit
) {
    try {
        LogManager.i(TAG, "=== STARTING PLAYLIST PARSING ===")
        LogManager.i(TAG, "URI: $uri")

        val playlist = withContext(Dispatchers.IO) {
            LogManager.d(TAG, "Calling parsePlaylistFromUri...")
            val result = musicScanner.parsePlaylistFromUri(uri, emptyList())
            LogManager.d(TAG, "parsePlaylistFromUri returned: ${result?.name ?: "null"}")
            result
        }

        LogManager.i(TAG, "Playlist parsing completed")

        if (playlist != null && playlist.songs.isNotEmpty()) {
            LogManager.i(TAG, "Playlist '${playlist.name}' has ${playlist.songs.size} songs")

            // 检查是否已存在同名播放列表
            val existingNames = existingPlaylists.map { it.name }
            if (playlist.name in existingNames) {
                LogManager.w(TAG, "Playlist ${playlist.name} already exists, adding suffix")
                onResult(playlist.copy(name = "${playlist.name} (1)"))
            } else {
                LogManager.i(TAG, "Adding new playlist: ${playlist.name}")
                onResult(playlist)
            }
        } else {
            LogManager.e(TAG, "No songs found in playlist or playlist is null")
            LogManager.e(TAG, "Playlist: ${playlist?.name}, Songs count: ${playlist?.songs?.size ?: 0}")
            onResult(null)
        }
    } catch (e: Exception) {
        LogManager.e(TAG, "Error importing playlist: ${e.message}", e)
        onResult(null)
    }
}
