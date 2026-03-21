/**
 * NewLibraryScreen.kt - Material 3 音乐库界面（性能优化版）
 *
 * 显示本地音乐库，包含特色播放列表、推荐艺术家和最近播放。
 * 使用绿色/黄色/白色配色方案的 Material 3 设计。
 * 
 * 性能优化：
 * - 移除嵌套的 LazyVerticalGrid，使用 LazyRow 替代
 * - 移除嵌套的 LazyColumn，使用 Column + forEach 替代
 * - 避免嵌套可滚动组件导致的滚动冲突
 */
package org.bibichan.union.player.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.bibichan.union.player.R
import org.bibichan.union.player.Song
import org.bibichan.union.player.data.MusicMetadata
import org.bibichan.union.player.ui.theme.*
import java.util.Locale

private const val TAG = "NewLibraryScreen"

/**
 * Material 3 音乐库主界面
 *
 * @param musicPlayer 音乐播放器实例
 * @param onNavigateToAlbum 导航到专辑详情
 * @param onNavigateToArtist 导航到艺术家详情
 * @param onRequestPermission 请求权限回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLibraryScreen(
    musicPlayer: Any, // MusicPlayer type
    onNavigateToAlbum: (String) -> Unit = {},
    onNavigateToArtist: (String) -> Unit = {},
    onRequestPermission: () -> Unit
) {
    val context = LocalContext.current
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var selectedTab by remember { mutableStateOf(0) }

    // 检查存储权限
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { /* User profile */ }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Mini Player Bar (persistent at bottom)
            MiniPlayerBar(
                currentTrack = null, // TODO: Pass current track
                isPlaying = false,
                onTap = { /* Expand to full player */ }
            )
        }
    ) { paddingValues ->
        if (!hasPermission) {
            PermissionRequiredCard(
                onRequestPermission = onRequestPermission,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Featured Playlists Section
                item {
                    FeaturedPlaylistsSection(
                        playlists = getSamplePlaylists(),
                        onPlaylistClick = { playlist ->
                            // Navigate to playlist detail
                        }
                    )
                }

                // Recommended Artists Section (性能优化：使用 LazyRow 替代 LazyVerticalGrid)
                item {
                    RecommendedArtistsSection(
                        artists = getSampleArtists(),
                        onArtistClick = { artist ->
                            onNavigateToArtist(artist.id)
                        },
                        onFollowClick = { artist ->
                            // Toggle follow
                        }
                    )
                }

                // Recently Played Section (性能优化：使用 Column 替代嵌套 LazyColumn)
                item {
                    RecentlyPlayedSection(
                        songs = songs.take(5),
                        onSongClick = { song ->
                            // Play song
                        }
                    )
                }
            }
        }
    }
}

/**
 * 特色播放列表部分
 */
@Composable
private fun FeaturedPlaylistsSection(
    playlists: List<FeaturedPlaylist>,
    onPlaylistClick: (FeaturedPlaylist) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Featured",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = { /* See all */ }) {
                Text("See All")
            }
        }

        // Horizontal scrollable playlists
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(playlists) { playlist ->
                FeaturedPlaylistCard(
                    playlist = playlist,
                    onClick = { onPlaylistClick(playlist) }
                )
            }
        }
    }
}

/**
 * 特色播放列表卡片
 */
@Composable
private fun FeaturedPlaylistCard(
    playlist: FeaturedPlaylist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .height(180.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = playlist.gradientColors
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Source badge
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text(playlist.source) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.align(Alignment.End)
                )

                // Title and artist
                Column(modifier = Modifier.align(Alignment.Start)) {
                    Text(
                        text = playlist.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = playlist.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * 推荐艺术家部分（性能优化：使用 LazyRow 替代 LazyVerticalGrid）
 */
@Composable
private fun RecommendedArtistsSection(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit,
    onFollowClick: (Artist) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedGenre by remember { mutableStateOf("All") }
    val genres = listOf("All", "Pop", "Rock", "Jazz", "Electronic")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Top Artists",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = { /* See all */ }) {
                Text("See All")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Genre filter chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genres) { genre ->
                FilterChip(
                    selected = selectedGenre == genre,
                    onClick = { selectedGenre = genre },
                    label = { Text(genre) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Featured artists - 使用 LazyRow 替代 LazyVerticalGrid 避免嵌套滚动冲突
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(artists.take(4)) { artist ->
                FeaturedArtistCard(
                    artist = artist,
                    onClick = { onArtistClick(artist) },
                    onFollowClick = { onFollowClick(artist) }
                )
            }
        }
    }
}

/**
 * 特色艺术家卡片（性能优化：固定宽度适配 LazyRow）
 */
@Composable
private fun FeaturedArtistCard(
    artist: Artist,
    onClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                if (artist.avatarUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artist.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Artist avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Artist name
            Text(
                text = artist.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Follow button
            OutlinedButton(
                onClick = onFollowClick,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (artist.isFollowing) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(
                    imageVector = if (artist.isFollowing) Icons.Default.Check else Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (artist.isFollowing) "Following" else "Follow",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

/**
 * 最近播放部分（性能优化：使用 Column 替代嵌套 LazyColumn）
 */
@Composable
private fun RecentlyPlayedSection(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recently Played",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = { /* See all */ }) {
                Text("See All")
            }
        }

        // Song list - 使用 Column 替代 LazyColumn 避免嵌套滾動衝突
        // 因為歌曲數量有限（最多5首），使用 Column 更高效
        songs.forEach { song ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                RecentSongItem(
                    song = song,
                    onClick = { onSongClick(song) }
                )
            }
        }
    }
}

/**
 * 最近播放歌曲项
 */
@Composable
private fun RecentSongItem(
    song: Song,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album art
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Song info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Play button
            FilledTonalButton(
                onClick = onClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Mini Player Bar（底部固定播放器）
 */
@Composable
private fun MiniPlayerBar(
    currentTrack: Song?,
    isPlaying: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentTrack == null) {
        // No track playing - show placeholder
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp),
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No music playing",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp),
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onTap() }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album art
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Album,
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Track info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentTrack.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentTrack.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Playback controls
                IconButton(onClick = { /* Previous */ }) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                FloatingActionButton(
                    onClick = { /* Toggle play/pause */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                IconButton(onClick = { /* Next */ }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * 权限请求卡片
 */
@Composable
private fun PermissionRequiredCard(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Storage Permission Required",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "To play local music files, we need access to your storage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(24.dp))

            FilledTonalButton(
                onClick = onRequestPermission,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Grant Permission")
            }
        }
    }
}

// ==================== Data Models ====================

/**
 * 特色播放列表
 */
data class FeaturedPlaylist(
    val id: String,
    val title: String,
    val artist: String,
    val source: String,
    val gradientColors: List<Color>,
    val artworkUrl: String? = null
)

/**
 * 艺术家
 */
data class Artist(
    val id: String,
    val name: String,
    val avatarUrl: String? = null,
    val genre: String = "Pop",
    val isFollowing: Boolean = false
)

/**
 * 曲目
 */
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val artwork: String? = null,
    val isPlaying: Boolean = false
)

// ==================== Sample Data ====================

/**
 * 获取示例播放列表
 */
private fun getSamplePlaylists(): List<FeaturedPlaylist> {
    return listOf(
        FeaturedPlaylist(
            id = "1",
            title = "DAWN",
            artist = "Yang",
            source = "Local",
            gradientColors = listOf(GreenPrimary, GreenLight)
        ),
        FeaturedPlaylist(
            id = "2",
            title = "誰か、海を。",
            artist = "Aimer",
            source = "Local",
            gradientColors = listOf(YellowPrimary, YellowLight)
        ),
        FeaturedPlaylist(
            id = "3",
            title = "Summer Vibes",
            artist = "Various Artists",
            source = "Spotify",
            gradientColors = listOf(GreenDark, YellowMuted)
        )
    )
}

/**
 * 获取示例艺术家
 */
private fun getSampleArtists(): List<Artist> {
    return listOf(
        Artist(
            id = "artist_1",
            name = "Taylor Swift",
            genre = "Pop",
            isFollowing = false
        ),
        Artist(
            id = "artist_2",
            name = "Ed Sheeran",
            genre = "Pop",
            isFollowing = true
        ),
        Artist(
            id = "artist_3",
            name = "Coldplay",
            genre = "Rock",
            isFollowing = false
        ),
        Artist(
            id = "artist_4",
            name = "Aimer",
            genre = "J-Pop",
            isFollowing = true
        )
    )
}