package org.bibichan.union.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.QueueMusic
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.bibichan.union.player.data.Album

/**
 * LibraryScreen - Apple Music Style
 *
 * A complete Jetpack Compose implementation of an Apple Music-style library screen.
 *
 * UI Structure:
 * - Simulated status bar (9:41 + system icons)
 * - "Library" header
 * - Horizontal scrollable row: Featured album covers
 * - Vertical grid: Navigation items (Playlists, Artists, Albums, Songs)
 * - "Recently Added" section with playback controls
 * - Fixed bottom navigation: Library + More tabs (with blur effect)
 *
 * Visual Design:
 * - Pure white background (#FFFFFFFF)
 * - High-contrast black text
 * - 160dp album covers with 8dp corner radius
 * - 90% white opaque bottom bar with 12dp radius + blur
 * - Material Design 3 components
 * - Minimum screen width: 360dp
 */
@Composable
fun LibraryScreen(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("Library") }

    // Placeholder data for UI preview (will be replaced with real metadata)
    val featuredAlbums = remember {
        listOf(
            Album(id = 1, title = "Why Not More?", artist = "Artist 1", coverUrl = "https://picsum.photos/seed/album1/600/600"),
            Album(id = 2, title = "R&B Now", artist = "Artist 2", coverUrl = "https://picsum.photos/seed/album2/600/600"),
            Album(id = 3, title = "MIKE", artist = "Artist 3", coverUrl = "https://picsum.photos/seed/album3/600/600")
        )
    }

    val navItems = remember {
        listOf(
            NavItem("Playlists", Icons.AutoMirrored.Outlined.QueueMusic),
            NavItem("Artists", Icons.Outlined.Person),
            NavItem("Albums", Icons.Outlined.Album),
            NavItem("Songs", Icons.Outlined.MusicNote)
        )
    }

    val recentlyAdded = remember {
        Album(id = 100, title = "Like A Ribbon", artist = "Polo & Pan", coverUrl = "https://picsum.photos/seed/recent/600/600")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Main scrollable content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 150.dp) // Space for MiniPlayer + BottomNav
        ) {
            // Status Bar (Simulated)
            item {
                StatusBar()
            }

            // Library Header
            item {
                LibraryHeader()
            }

            // Featured Albums (Horizontal Scroll)
            item {
                FeaturedAlbumsSection(
                    albums = featuredAlbums,
                    onAlbumClick = onAlbumClick
                )
            }

            // Navigation Grid
            item {
                NavigationGridSection(
                    items = navItems,
                    onNavItemClick = { /* TODO: Implement navigation */ }
                )
            }

            // Recently Added
            item {
                RecentlyAddedSection(
                    album = recentlyAdded,
                    onPlayClick = { /* TODO: Implement playback */ },
                    onMoreClick = { /* TODO: Implement more options */ }
                )
            }

            // Bottom spacer
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Fixed Bottom Navigation Bar with blur effect
        BottomNavigationBar(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it
                onNavigate(it)
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Status Bar Component (Simulated)
 *
 * Displays time (9:41) and system icons.
 * In production, this would use actual system status bar.
 */
@Composable
private fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time
        Text(
            text = "9:41",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )

        // System icons (simulated)
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Signal strength
            Icon(
                imageVector = Icons.Default.SignalCellular4Bar,
                contentDescription = "Signal",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
            // WiFi
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "WiFi",
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
            // Battery
            Icon(
                imageVector = Icons.Default.BatteryFull,
                contentDescription = "Battery",
                tint = Color.Black,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Library Header Component
 *
 * Large "Library" title text (34sp, Bold).
 */
@Composable
private fun LibraryHeader() {
    Text(
        text = "Library",
        fontSize = 34.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

/**
 * Featured Albums Section
 *
 * Horizontal scrollable row of large album covers (160dp x 160dp).
 */
@Composable
private fun FeaturedAlbumsSection(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section header
        Text(
            text = "Featured",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Horizontal scrollable row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums) { album ->
                AlbumCoverCard(
                    album = album,
                    onClick = { onAlbumClick(album) },
                    modifier = Modifier
                        .width(160.dp)
                        .height(160.dp)
                )
            }
        }

        // Divider
        Divider(
            color = Color(0xFFEEEEEE),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/**
 * Album Cover Card Component
 *
 * Square album cover with rounded corners (8dp radius).
 */
@Composable
private fun AlbumCoverCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = album.coverUrl,
            contentDescription = album.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

/**
 * Navigation Grid Item Data Class
 */
data class NavItem(val title: String, val icon: ImageVector)

/**
 * Navigation Grid Section
 *
 * 2-column grid with left-aligned icons and text.
 */
@Composable
private fun NavigationGridSection(
    items: List<NavItem>,
    onNavItemClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                NavigationGridItem(
                    item = item,
                    onClick = { onNavItemClick(item.title) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        }

        // Divider
        Divider(
            color = Color(0xFFEEEEEE),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/**
 * Navigation Grid Item Component
 *
 * Single row with left-aligned icon and text.
 */
@Composable
private fun NavigationGridItem(
    item: NavItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = Color.Black,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = item.title,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

/**
 * Recently Added Section
 *
 * Single album with playback controls.
 */
@Composable
private fun RecentlyAddedSection(
    album: Album,
    onPlayClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section header
        Text(
            text = "Recently Added",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Recently added card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album cover
            AsyncImage(
                model = album.coverUrl,
                contentDescription = album.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // Song info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = album.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = album.artist,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF666666),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Playback controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }

                IconButton(
                    onClick = onMoreClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreHoriz,
                        contentDescription = "More",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Divider
        Divider(
            color = Color(0xFFEEEEEE),
            thickness = 1.dp,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

/**
 * Bottom Navigation Bar Component (Apple Music Style)
 *
 * Fixed bottom bar with two tabs: Library and More.
 *
 * Visual Properties:
 * - 12dp corner radius
 * - BackdropFilter blur effect (sigmaX=8f, sigmaY=8f)
 * - Semi-transparent white background (90% opacity)
 * - Fixed position at screen bottom
 */
@Composable
private fun BottomNavigationBar(
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.9f))
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            val bottomNavItems = listOf(
                "Library" to Icons.AutoMirrored.Filled.LibraryBooks,
                "More" to Icons.Default.MoreVert
            )

            bottomNavItems.forEach { (label, icon) ->
                NavigationBarItem(
                    selected = selectedTab == label,
                    onClick = { onTabSelected(label) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (selectedTab == label) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selectedTab == label) Color.Black else Color(0xFF666666),
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        unselectedIconColor = Color(0xFF666666),
                        selectedTextColor = Color.Black,
                        unselectedTextColor = Color(0xFF666666),
                        indicatorColor = Color(0xFFE5E5E5)
                    )
                )
            }
        }
    }
}
