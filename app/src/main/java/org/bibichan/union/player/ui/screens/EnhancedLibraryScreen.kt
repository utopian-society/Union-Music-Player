/**
 * EnhancedLibraryScreen.kt - 增强版音乐库界面
 *
 * 显示扫描后的音乐文件，支持按专辑、艺术家分组
 */
package org.bibichan.union.player.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.data.MusicLibraryViewModel
import org.bibichan.union.player.data.MusicScanner
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedLibraryScreen(
    onSongClick: (MusicMetadata) -> Unit,
    viewModel: MusicLibraryViewModel = viewModel()
) {
    val context = LocalContext.current
    val musicLibrary by viewModel.musicLibrary.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val libraryStats by viewModel.libraryStats.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All Songs", "Albums", "Artists")
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, "Search")
                    }
                    IconButton(onClick = { /* TODO: Settings */ }) {
                        Icon(Icons.Default.MoreVert, "More")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 显示扫描状态
            when (val state = scanState) {
                is MusicScanner.ScanState.Scanning -> {
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Scanning: ${state.currentFile}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                is MusicScanner.ScanState.Completed -> {
                    if (state.result.songs.isNotEmpty()) {
                        Text(
                            text = "${state.result.songs.size} songs found in ${state.result.scanTime}ms",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is MusicScanner.ScanState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {}
            }
            
            // 统计信息
            if (libraryStats.totalSongs > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.MusicNote,
                        label = "Songs",
                        value = libraryStats.totalSongs.toString()
                    )
                    StatItem(
                        icon = Icons.Default.Person,
                        label = "Artists",
                        value = libraryStats.totalArtists.toString()
                    )
                    StatItem(
                        icon = Icons.Default.Album,
                        label = "Albums",
                        value = libraryStats.totalAlbums.toString()
                    )
                }
            }
            
            // 标签页
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            // 内容区域
            when (selectedTab) {
                0 -> AllSongsList(
                    songs = musicLibrary,
                    onSongClick = onSongClick
                )
                1 -> AlbumsList(
                    songs = musicLibrary,
                    onAlbumClick = { /* TODO: Navigate to album detail */ }
                )
                2 -> ArtistsList(
                    songs = musicLibrary,
                    onArtistClick = { /* TODO: Navigate to artist detail */ }
                )
            }
        }
    }
}

@Composable
fun AllSongsList(
    songs: List<MusicMetadata>,
    onSongClick: (MusicMetadata) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(songs) { song ->
            SongItem(
                song = song,
                onClick = { onSongClick(song) }
            )
        }
    }
}

@Composable
fun AlbumsList(
    songs: List<MusicMetadata>,
    onAlbumClick: (String) -> Unit
) {
    val albums = songs.groupBy { it.album }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(albums.entries.toList()) { (album, songsInAlbum) ->
            AlbumCard(
                album = album,
                songs = songsInAlbum,
                onClick = { onAlbumClick(album) }
            )
        }
    }
}

@Composable
fun ArtistsList(
    songs: List<MusicMetadata>,
    onArtistClick: (String) -> Unit
) {
    val artists = songs.groupBy { it.artist }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(artists.entries.toList()) { (artist, songsByArtist) ->
            ArtistCard(
                artist = artist,
                songs = songsByArtist,
                onClick = { onArtistClick(artist) }
            )
        }
    }
}

@Composable
fun SongItem(
    song: MusicMetadata,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 专辑封面
            AlbumArtThumbnail(song = song)
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Text(
                    text = "${song.album} • ${formatDuration(song.duration)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            
            // 格式标签
            Surface(
                modifier = Modifier.padding(start = 8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = song.format.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AlbumArtThumbnail(song: MusicMetadata) {
    Surface(
        modifier = Modifier.size(56.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        song.albumArt?.let { bitmap ->
            // 显示专辑封面
            androidx.compose.foundation.Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: run {
            // 显示占位符
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AlbumCard(
    album: String,
    songs: List<MusicMetadata>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            // 专辑封面（使用第一首歌的封面）
            songs.firstOrNull()?.albumArt?.let { bitmap ->
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Album Art",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } ?: run {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        modifier = Modifier.padding(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = album,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2
                )
                Text(
                    text = "${songs.size} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = songs.firstOrNull()?.artist ?: "Various Artists",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ArtistCard(
    artist: String,
    songs: List<MusicMetadata>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
            Surface(
                modifier = Modifier.size(80.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = artist,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1
                )
                Text(
                    text = "${songs.size} songs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${songs.map { it.album }.distinct().size} albums",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatDuration(milliseconds: Long): String {
    val seconds = (milliseconds / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%d:%02d", minutes, remainingSeconds)
}
