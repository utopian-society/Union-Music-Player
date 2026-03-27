/**
 * UnionMusicApp.kt - 主应用 Composable
 *
 * 这是应用的主 Composable，包含浮动播放器和底部导航栏。
 * 设计参考 Apple Music 风格：Library、Playlist、Files、More 四个主要功能。
 * 使用 Material 3 的 NavigationBar 和 Scaffold 实现标准导航。
 *
 * 2026-03-22: 新增 Files 標籤頁
 * 2026-03-24: 使用 StateFlow 驅動浮動播放器可見性
 * 2026-03-24: 浮動播放器常駐
 */
package org.bibichan.union.player.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.data.MusicScanner
import org.bibichan.union.player.data.Playlist
import org.bibichan.union.player.ui.components.BottomControlPanel
import org.bibichan.union.player.ui.components.FloatingPlayer
import org.bibichan.union.player.ui.components.FullPlayerSheetContent
import org.bibichan.union.player.ui.components.LogManager
import org.bibichan.union.player.ui.components.NavItem
import org.bibichan.union.player.ui.library.LibraryScreen
import org.bibichan.union.player.ui.library.LibraryViewModel
import org.bibichan.union.player.ui.library.data.Album
import org.bibichan.union.player.ui.screens.AlbumDetailScreen
import org.bibichan.union.player.ui.screens.FilesScreen
import org.bibichan.union.player.ui.screens.MoreScreen
import org.bibichan.union.player.ui.screens.PlaylistManagementScreen

private const val TAG = "UnionMusicApp"

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

    var selectedTab by remember { mutableStateOf(0) }
    var selectedAlbum by remember { mutableStateOf<Album?>(null) }
    var importedPlaylists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var showPlayerOverlay by remember { mutableStateOf(false) }
    var miniPlayerBounds by remember { mutableStateOf<Rect?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val musicScanner = remember { MusicScanner(context) }

    val libraryViewModel: LibraryViewModel = viewModel()
    val albums by libraryViewModel.albums.collectAsState()

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

    val density = LocalDensity.current
    val expandProgress by animateFloatAsState(
        targetValue = if (showPlayerOverlay) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "playerExpand"
    )

    Scaffold(
        bottomBar = {
            Column {
                FloatingPlayer(
                    musicPlayer = musicPlayer,
                    onExpand = {
                        if (!showPlayerOverlay) {
                            showPlayerOverlay = true
                        }
                    },
                    modifier = Modifier.alpha(1f - expandProgress),
                    onBoundsChanged = { bounds -> miniPlayerBounds = bounds }
                )
                BottomControlPanel(
                    items = navItems,
                    selectedIndex = selectedTab,
                    onItemSelected = { index -> selectedTab = index }
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                selectedAlbum != null -> {
                    AlbumDetailScreen(
                        album = selectedAlbum!!,
                        onSongClick = { _, playlist, index ->
                            playSongList(musicPlayer, playlist, index)
                            showPlayerOverlay = true
                        },
                        onBack = {
                            selectedAlbum = null
                        }
                    )
                }

                else -> when (selectedTab) {
                    0 -> LibraryScreen(
                        musicPlayer = musicPlayer,
                        onAlbumClick = { albumId ->
                            selectedAlbum = albums.firstOrNull { it.id == albumId }
                        },
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
                            if (playlist.songs.isNotEmpty()) {
                                LogManager.i(TAG, "Playing playlist: ${playlist.name} with ${playlist.songs.size} songs")
                                playSongList(musicPlayer, playlist.songs, 0)
                                showPlayerOverlay = true
                            }
                        },
                        onPlaylistDelete = { playlist ->
                            LogManager.i(TAG, "Deleting playlist: ${playlist.name}")
                            importedPlaylists = importedPlaylists.filter { it.id != playlist.id }
                        }
                    )

                    2 -> FilesScreen(
                        musicPlayer = musicPlayer,
                        onFolderPickerRequest = onFolderPickerRequest,
                        onOpenPlayer = { showPlayerOverlay = true }
                    )

                    3 -> MoreScreen(
                        onRequestPermission = onRequestPermission,
                        onPermissionResult = onPermissionResult,
                        onFolderPickerRequest = onFolderPickerRequest
                    )
                }
            }
        }

        if (expandProgress > 0f && miniPlayerBounds != null) {
            val bounds = miniPlayerBounds
            val screenWidth = with(density) { LocalContext.current.resources.displayMetrics.widthPixels.toFloat() }
            val screenHeight = with(density) { LocalContext.current.resources.displayMetrics.heightPixels.toFloat() }
            val insetPx = with(density) { 16.dp.toPx() }

            val targetWidth = (screenWidth - insetPx * 2f).coerceAtLeast(0f)
            val targetHeight = (screenHeight - insetPx * 2f).coerceAtLeast(0f)
            val targetX = insetPx
            val targetTop = insetPx

            val startWidth = bounds?.width ?: targetWidth
            val startHeight = bounds?.height ?: targetHeight
            val startX = bounds?.left ?: targetX
            val startTop = bounds?.top ?: targetTop

            val width = startWidth + (targetWidth - startWidth) * expandProgress
            val height = startHeight + (targetHeight - startHeight) * expandProgress
            val x = startX + (targetX - startX) * expandProgress
            val y = startTop + (targetTop - startTop) * expandProgress + dragOffsetY

            val widthDp = with(density) { width.toDp() }
            val heightDp = with(density) { height.toDp() }
            val cornerRadius = androidx.compose.ui.unit.lerp(20.dp, 32.dp, expandProgress)
            val shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius)

            BackHandler(enabled = showPlayerOverlay) {
                showPlayerOverlay = false
                dragOffsetY = 0f
            }

            Box(
                modifier = Modifier
                    .offset { IntOffset(x.toInt(), y.toInt()) }
                    .size(widthDp, heightDp)
                    .clip(shape)
                    .shadow(12.dp, shape, clip = false)
                    .pointerInput(showPlayerOverlay) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, dragAmount ->
                                if (showPlayerOverlay) {
                                    dragOffsetY = (dragOffsetY + dragAmount).coerceAtLeast(0f)
                                }
                            },
                            onDragEnd = {
                                if (dragOffsetY > screenHeight * 0.2f) {
                                    showPlayerOverlay = false
                                }
                                dragOffsetY = 0f
                            }
                        )
                    }
            ) {
                FullPlayerSheetContent(
                    musicPlayer = musicPlayer,
                    onCollapse = {
                        showPlayerOverlay = false
                        dragOffsetY = 0f
                    },
                    modifier = Modifier
                        .offset { IntOffset(0, 0) }
                        .fillMaxSize(),
                    expandProgress = expandProgress,
                    collapseDragOffsetY = 0f
                )
            }
        }
    }
}

private fun playSongList(
    musicPlayer: MusicPlayer,
    songs: List<MusicMetadata>,
    startIndex: Int
) {
    musicPlayer.setSongs(songs)
    musicPlayer.play(startIndex)

    LogManager.i(TAG, "Playing song at index $startIndex from list of ${songs.size} songs")

    songs.forEachIndexed { index, song ->
        LogManager.d(TAG, "Song $index: ${song.title} - ${song.artist}")
    }
}

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
