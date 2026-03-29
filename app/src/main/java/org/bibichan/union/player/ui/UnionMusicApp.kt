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
 * 2026-03-28: 使用 BottomSheetScaffold 替代自定义浮动层
 */
package org.bibichan.union.player.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.data.MusicScanner
import org.bibichan.union.player.data.Playlist
import org.bibichan.union.player.ui.components.BottomControlPanel
import org.bibichan.union.player.ui.components.LogManager
import org.bibichan.union.player.ui.components.NavItem
import org.bibichan.union.player.ui.library.LibraryScreen
import org.bibichan.union.player.ui.library.LibraryViewModel
import org.bibichan.union.player.ui.library.data.Album
import org.bibichan.union.player.ui.player.PlayerScreen
import org.bibichan.union.player.ui.player.PlayerViewModel
import org.bibichan.union.player.ui.player.PlayerViewModelFactory
import org.bibichan.union.player.ui.screens.AlbumDetailScreen
import org.bibichan.union.player.ui.screens.FilesScreen
import org.bibichan.union.player.ui.screens.MoreScreen
import org.bibichan.union.player.ui.screens.PlaylistManagementScreen

private val BottomBarHeight = 72.dp

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

    val musicScanner = remember { MusicScanner(context) }

    val libraryViewModel: LibraryViewModel = viewModel()
    val albums by libraryViewModel.albums.collectAsState()

    val playerViewModel: PlayerViewModel = viewModel(
        factory = PlayerViewModelFactory(musicPlayer, context)
    )

    val hazeState = rememberHazeState()

    val sheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
        skipHiddenState = true,
        confirmValueChange = { targetValue ->
            if (targetValue == SheetValue.Hidden) {
                return@rememberStandardBottomSheetState false
            }
            true
        }
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )

    val isSheetExpanded = sheetState.targetValue == SheetValue.Expanded
    val bottomInsetTarget = if (isSheetExpanded) 0.dp else BottomBarHeight
    val bottomInset by animateDpAsState(label = "sheetBottomInset", targetValue = bottomInsetTarget)

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

    BackHandler(enabled = isSheetExpanded) {
        scope.launch { sheetState.partialExpand() }
    }

    Scaffold(
        bottomBar = {
            if (!isSheetExpanded) {
                BottomControlPanel(
                    items = navItems,
                    selectedIndex = selectedTab,
                    onItemSelected = { index -> selectedTab = index }
                )
            }
        }
    ) { outerPadding ->
        BottomSheetScaffold(
            modifier = Modifier.padding(bottom = bottomInset),
            scaffoldState = scaffoldState,
            sheetPeekHeight = 72.dp,
            sheetShadowElevation = 0.dp,
            sheetContainerColor = Color.Transparent,
            sheetDragHandle = {
                if (isSheetExpanded) {
                    BottomSheetDefaults.DragHandle()
                }
            },
            sheetContent = {
                Column(modifier = Modifier.hazeSource(state = hazeState)) {
                    PlayerScreen(
                        viewModel = playerViewModel,
                        isExpanded = isSheetExpanded,
                        onExpand = { scope.launch { sheetState.expand() } },
                        onCollapse = { scope.launch { sheetState.partialExpand() } },
                        hazeState = hazeState,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(outerPadding)
                    .padding(innerPadding)
            ) {
                when {
                    selectedAlbum != null -> {
                        AlbumDetailScreen(
                            album = selectedAlbum!!,
                            onSongClick = { _, playlist, index ->
                                playSongList(musicPlayer, playlist, index)
                                scope.launch { sheetState.expand() }
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
                                    scope.launch { sheetState.expand() }
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
                            onOpenPlayer = { scope.launch { sheetState.expand() } }
                        )

                        3 -> MoreScreen(
                            onRequestPermission = onRequestPermission,
                            onPermissionResult = onPermissionResult,
                            onFolderPickerRequest = onFolderPickerRequest
                        )
                    }
                }
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
