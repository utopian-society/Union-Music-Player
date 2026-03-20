/**
 * LibraryScreen.kt - Material 3 Redesigned Music Library Interface (Performance Optimized)
 *
 * Performance Optimizations:
 * - Removed nested Lazy layouts (LazyColumn containing LazyVerticalGrid)
 * - Used derivedStateOf for search filtering
 * - Cached gradient brushes with remember
 * - Simplified album cards for better performance
 * - Optimized image loading with placeholders
 */

package org.bibichan.union.player.ui.library

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.bibichan.union.player.ui.library.components.AlbumCard
import org.bibichan.union.player.ui.library.data.Album

private const val TAG = "LibraryScreen"

/**
 * Material 3 Redesigned Library Screen (Performance Optimized)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onAlbumClick: (String) -> Unit = { albumId -> Log.i(TAG, "Selected album: $albumId") },
    onRequestPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel: LibraryViewModel = viewModel()
    val albums by viewModel.albums.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val hasPermission by viewModel.hasPermission.collectAsState()
    val topArtists by viewModel.topArtists.collectAsState()

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Performance optimization: Use derivedStateOf for search filtering
    val filteredAlbums by remember { derivedStateOf {
        if (searchQuery.isBlank()) {
            albums
        } else {
            albums.filter { album ->
                album.title.contains(searchQuery, ignoreCase = true) ||
                album.artistName.contains(searchQuery, ignoreCase = true)
            }
        }
    }}

    Scaffold(
        topBar = {
            LibraryTopAppBar(
                isSearching = isSearching,
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onSearchToggle = { isSearching = it },
                onRefresh = { viewModel.refresh() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                !hasPermission -> {
                    PermissionRequiredContent(onRequestPermission = onRequestPermission)
                }
                isLoading -> {
                    LoadingContent()
                }
                error != null -> {
                    ErrorContent(
                        message = error ?: "Unknown error",
                        onRetry = { viewModel.refresh() }
                    )
                }
                else -> {
                    OptimizedLibraryContent(
                        albums = filteredAlbums,
                        topArtists = topArtists,
                        onAlbumClick = onAlbumClick,
                        searchQuery = searchQuery
                    )
                }
            }
        }
    }
}

/**
 * Top App Bar with search functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopAppBar(
    isSearching: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = {
            AnimatedVisibility(
                visible = isSearching,
                enter = fadeIn(animationSpec = tween(200)),
                exit = fadeOut(animationSpec = tween(200))
            ) {
                if (isSearching) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                "Search albums, artists...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (!isSearching) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LibraryMusic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Library",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { onSearchToggle(!isSearching) }) {
                Icon(
                    if (isSearching) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (isSearching) "Close search" else "Search",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            if (!isSearching) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

/**
 * Optimized Library Content - NO NESTED LAZY LAYOUTS
 */
@Composable
private fun OptimizedLibraryContent(
    albums: List<Album>,
    topArtists: List<ArtistData>,
    onAlbumClick: (String) -> Unit,
    searchQuery: String
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = 8.dp,
            bottom = 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Featured Section Header
        if (searchQuery.isBlank() && albums.isNotEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                SectionHeader(
                    title = "Featured",
                    subtitle = "Popular albums",
                    showSeeAll = true
                )
            }

            // Featured Albums (Horizontal scroll in a grid item)
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                FeaturedAlbumsRow(
                    albums = albums.take(5),
                    onAlbumClick = onAlbumClick
                )
            }
        }

        // Top Artists Section
        if (searchQuery.isBlank() && topArtists.isNotEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                Column {
                    SectionHeader(
                        title = "Top Artists",
                        subtitle = "${topArtists.size} artists",
                        showSeeAll = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ArtistsRow(
                        artists = topArtists,
                        onArtistClick = { artist ->
                            Log.i(TAG, "Artist clicked: $artist")
                        }
                    )
                }
            }
        }

        // All Albums Section Header
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            SectionHeader(
                title = if (searchQuery.isNotBlank()) "Search Results" else "All Albums",
                subtitle = "${albums.size} albums",
                showSeeAll = false
            )
        }

        // Album Grid Items
        if (albums.isEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
                EmptyStateContent(
                    searchQuery = searchQuery,
                    onScanFiles = { }
                )
            }
        } else {
            items(
                items = albums,
                key = { it.id }
            ) { album ->
                AlbumCard(
                    album = album,
                    onClick = { onAlbumClick(album.id) }
                )
            }
        }
    }
}

/**
 * Featured Albums Row - Horizontal scrollable section
 */
@Composable
private fun FeaturedAlbumsRow(
    albums: List<Album>,
    onAlbumClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(albums) { album ->
            FeaturedPlaylistCard(
                album = album,
                onClick = { onAlbumClick(album.id) }
            )
        }
    }
}

/**
 * Featured Playlist Card - Optimized with cached gradient
 */
@Composable
private fun FeaturedPlaylistCard(
    album: Album,
    onClick: () -> Unit
) {
    // Get theme colors first (outside remember)
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer

    // Cache gradient brush to avoid recreation
    val gradientBrush = remember(primaryColor, secondaryColor, primaryContainerColor) {
        Brush.linearGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.7f),
                secondaryColor.copy(alpha = 0.5f),
                primaryContainerColor.copy(alpha = 0.8f)
            )
        )
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(280.dp)
            .height(200.dp),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background with cached gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = gradientBrush)
            )

            // Album artwork
            if (album.artworkUri != null) {
                AsyncImage(
                    model = album.artworkUri,
                    contentDescription = album.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.3f
                )
            }

            // Content overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Album count badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "${album.songCount} songs",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Title and artist
                Column {
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = album.artistName,
                        style = MaterialTheme.typography.bodyLarge,
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
 * Artists Row - Horizontal scrollable section
 */
@Composable
private fun ArtistsRow(
    artists: List<ArtistData>,
    onArtistClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(artists.take(5)) { artist ->
            ArtistChip(
                artist = artist,
                onClick = { onArtistClick(artist.name) }
            )
        }
    }
}

/**
 * Artist Chip Component - Simplified for performance
 */
@Composable
private fun ArtistChip(
    artist: ArtistData,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Text(
                text = artist.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge
            )
        },
        leadingIcon = {
            ArtistAvatar(
                artist = artist,
                size = 24.dp
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline,
            enabled = true,
            selected = false
        )
    )
}

/**
 * Artist Avatar Component - Optimized
 */
@Composable
private fun ArtistAvatar(
    artist: ArtistData,
    size: androidx.compose.ui.unit.Dp,
    borderWidth: androidx.compose.ui.unit.Dp = 2.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .border(
                width = borderWidth,
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        if (artist.artworkUri != null) {
            AsyncImage(
                model = artist.artworkUri,
                contentDescription = artist.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(size * 0.6f)
            )
        }
    }
}

/**
 * Section Header Component
 */
@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    showSeeAll: Boolean = false,
    onSeeAllClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showSeeAll) {
            TextButton(onClick = onSeeAllClick) {
                Text(
                    "See All",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Permission Required Content
 */
@Composable
private fun PermissionRequiredContent(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
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
                    text = "To display your music library, we need access to your storage.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(
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
}

/**
 * Loading Content
 */
@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading your music library...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Error Content
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error loading library",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            FilledTonalButton(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}

/**
 * Empty State Content
 */
@Composable
private fun EmptyStateContent(
    searchQuery: String,
    onScanFiles: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (searchQuery.isNotBlank()) {
                    "No albums found"
                } else {
                    "No Music Found"
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (searchQuery.isNotBlank()) {
                    "Try a different search term"
                } else {
                    "Add some music to your device to start playing"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (searchQuery.isBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                FilledTonalButton(onClick = onScanFiles) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan Files")
                }
            }
        }
    }
}
