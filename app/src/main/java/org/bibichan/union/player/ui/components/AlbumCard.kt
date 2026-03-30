package org.bibichan.union.player.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.bibichan.union.player.data.Album

/**
 * AlbumCard Component
 * 
 * Displays a single album as a card with:
 * - Album cover image (square, rounded corners)
 * - Album title
 * - Artist name
 * 
 * KEY CONCEPTS:
 * 
 * 1. Card:
 *    - Material Design container with elevation (shadow)
 *    - Can handle click interactions
 *    - Automatically adds ripple effect on touch
 * 
 * 2. AspectRatio:
 *    - Constrains widget to specific width:height ratio
 *    - aspectRatio(1f) = 1:1 ratio = perfect square
 * 
 * 3. AsyncImage (from Coil library):
 *    - Loads images asynchronously (doesn't block UI)
 *    - Automatically caches images
 *    - Handles loading and error states
 * 
 * 4. Clip:
 *    - Cuts content to a specific shape
 *    - Used here to round image corners
 * 
 * 5. ContentScale:
 *    - Controls how image fills its container
 *    - Crop: Scales image to fill, may cut edges
 */
@Composable
fun AlbumCard(
    // PARAMETERS:
    // - album: The album data to display
    // - onClick: Callback when card is tapped (receives Album as parameter)
    // - modifier: Optional external styling
    album: Album,
    onClick: (Album) -> Unit,
    modifier: Modifier = Modifier
) {
    // Card: Material Design container with shadow and click handling
    Card(
        // onClick: Called when user taps the card
        onClick = { onClick(album) },  // Pass album data to parent

        // Styling the card
        // Make the card square (1:1 aspect ratio) by applying aspectRatio to modifier
        modifier = modifier
            .padding(8.dp)      // Add 8dp space around card
            .fillMaxWidth()     // Fill available width
            .aspectRatio(1f),   // 1:1 ratio = perfect square

        // Rounded corners (12dp radius)
        shape = RoundedCornerShape(12.dp),

        // Elevation creates drop shadow effect
        // defaultElevation = 4dp = subtle shadow
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),

        // Card background color
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        // Column: Stack image and info vertically
        Column {
            // ─────────────────────────────────────────────────────
            // ALBUM COVER IMAGE
            // ─────────────────────────────────────────────────────
            AsyncImage(
                // model: What to load (URL, file path, or resource ID)
                model = album.coverUrl,
                
                // contentDescription: For accessibility (screen readers)
                contentDescription = album.title,
                
                // Styling the image
                modifier = Modifier
                    .fillMaxWidth()    // Full card width
                    .weight(1f),       // Take all remaining vertical space
                
                // Clip the top corners to match card shape
                // topStart = top-left, topEnd = top-right
                contentScale = ContentScale.Crop  // Crop to fill square
            )
            
            // ─────────────────────────────────────────────────────
            // ALBUM INFO (title and artist)
            // ─────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()    // Full card width
                    .padding(12.dp)    // 12dp padding inside card
            ) {
                // Album title
                Text(
                    text = album.title,  // Display album's title
                    // Use theme's titleMedium style
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,  // Single line only
                    // Ellipsis (...) if text is too long
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                // Artist name (smaller, secondary text)
                Text(
                    text = album.artist,  // Display album's artist
                    // Smaller text style for secondary info
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,  // Single line only
                    // Gray color for less prominent text
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * WHY USE weight(1f) ON THE IMAGE?
 * 
 * Without weight:
 * ┌─────────────────┐
 * │ [small image]   │ ← Image only takes its natural size
 * │ [title]         │
 * │ [artist]        │
 * └─────────────────┘
 * 
 * With weight(1f):
 * ┌─────────────────┐
 * │                 │
 * │                 │ ← Image expands to fill space
 * │   [big image]   │
 * │                 │
 * ├─────────────────┤
 * │ [title]         │ ← Text only takes what it needs
 * │ [artist]        │
 * └─────────────────┘
 */
