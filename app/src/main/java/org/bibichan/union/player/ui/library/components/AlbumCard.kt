/**
 * AlbumCard.kt - Material 3 Enhanced Album Card Component (Performance Optimized)
 * 
 * Performance Optimizations:
 * - Removed AssistChip (heavy component)
 * - Simplified gradient rendering
 * - Reduced elevation usage
 * - Optimized image loading
 */
package org.bibichan.union.player.ui.library.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import coil.compose.AsyncImage
import org.bibichan.union.player.ui.library.data.Album

/**
 * Material 3 Album Card - Performance Optimized
 */
@Composable
fun AlbumCard(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Album artwork - 1:1 ratio
            OptimizedAlbumArtwork(
                artworkUri = album.artworkUri,
                albumTitle = album.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Album title
            Text(
                text = album.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // Artist name
            Text(
                text = album.artistName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Song count and year - Simplified (no AssistChip)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Song count badge - Simple surface instead of AssistChip
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.height(20.dp)
                ) {
                    Text(
                        text = "${album.songCount} songs",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                
                album.year?.let { year ->
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• $year",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Optimized Album Artwork - Simplified gradient
 */
@Composable
fun OptimizedAlbumArtwork(
    artworkUri: Uri?,
    albumTitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (artworkUri != null) {
                // Load artwork with Coil
                AsyncImage(
                    model = artworkUri,
                    contentDescription = "Album art for $albumTitle",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Simplified placeholder - single gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "No album art",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Enhanced Album Artwork (for compatibility)
 */
@Composable
fun EnhancedAlbumArtwork(
    artworkUri: Uri?,
    albumTitle: String,
    modifier: Modifier = Modifier
) {
    OptimizedAlbumArtwork(
        artworkUri = artworkUri,
        albumTitle = albumTitle,
        modifier = modifier
    )
}

/**
 * Original Album Artwork Component (for compatibility)
 */
@Composable
fun AlbumArtwork(
    artworkUri: Uri?,
    albumTitle: String,
    modifier: Modifier = Modifier
) {
    OptimizedAlbumArtwork(
        artworkUri = artworkUri,
        albumTitle = albumTitle,
        modifier = modifier
    )
}

/**
 * Compact Album Card for lists
 */
@Composable
fun AlbumCardCompact(
    album: Album,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Small artwork thumbnail
            OptimizedAlbumArtwork(
                artworkUri = album.artworkUri,
                albumTitle = album.title,
                modifier = Modifier.size(56.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Album info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = album.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Song count
            Text(
                text = "${album.songCount} songs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
