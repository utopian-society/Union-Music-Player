package org.bibichan.union.player.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.bibichan.union.player.data.Album
import org.bibichan.union.player.ui.components.AlbumCard

/**
 * LibraryScreen Component
 * 
 * The main library screen that displays:
 * - "精选推荐" (Featured) header
 * - Grid of album cards
 * 
 * KEY CONCEPTS:
 * 
 * 1. LazyVerticalGrid:
 *    - Efficient grid that only renders visible items
 *    - Like RecyclerView but for Compose
 *    - "Lazy" = loads items on demand as you scroll
 * 
 * 2. GridCells:
 *    - Defines how many columns in the grid
 *    - GridCells.Fixed(2) = always 2 columns
 *    - GridCells.Adaptive(minSize) = as many as fit (each at least minSize wide)
 * 
 * 3. items():
 *    - Tells the grid how many items to display
 *    - Creates a composable for each item
 * 
 * 4. @OptIn(ExperimentalLayoutApi::class):
 *    - Some Compose APIs are "experimental" (may change)
 *    - @OptIn suppresses the warning
 *    - Safe to use for production
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    // PARAMETERS:
    // - albums: List of albums to display
    // - onAlbumClick: Callback when an album card is tapped
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit
) {
    // Column: Stack header and grid vertically
    Column(
        // Add padding around the entire screen
        modifier = Modifier
            .fillMaxSize()      // Fill entire available area
            .padding(16.dp)     // 16dp padding on all sides
    ) {
        // ─────────────────────────────────────────────────────
        // HEADER: "精选推荐" (Featured/Recommended)
        // ─────────────────────────────────────────────────────
        Text(
            text = "精选推荐",  // Chinese for "Featured Recommendations"
            // Use large, bold title style from theme
            style = MaterialTheme.typography.headlineMedium,
            // Add space below the header (before the grid)
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // ─────────────────────────────────────────────────────
        // ALBUM GRID
        // ─────────────────────────────────────────────────────
        LazyVerticalGrid(
            // Define column configuration:
            // GridCells.Adaptive(minSize = 180.dp) means:
            // - Each column is at least 180dp wide
            // - As many columns as fit horizontally
            // - On phone: probably 2 columns
            // - On tablet: probably 4-5 columns
            columns = GridCells.Adaptive(minSize = 180.dp),
            
            // Add space between grid items
            contentPadding = PaddingValues(bottom = 80.dp),  // Space for MiniPlayer
            
            // Vertical spacing between rows
            verticalArrangement = Arrangement.spacedBy(16.dp),
            
            // Horizontal spacing between columns
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // items(): Create a composable for each album in the list
            items(
                count = albums.size,  // How many items total
                key = { index -> albums[index].id }  // Unique key for each item (helps with animations)
            ) { index ->
                // Get the album at this position
                val album = albums[index]
                
                // Display the album card
                AlbumCard(
                    album = album,  // Pass album data
                    onClick = onAlbumClick  // Pass click callback
                )
            }
        }
    }
}

/**
 * WHY USE LAZYVERTICALGRID INSTEAD OF REGULAR COLUMN?
 * 
 * Regular Column with 100 album cards:
 * - Creates ALL 100 cards at once
 * - Uses lots of memory
 * - Slow to render
 * - Phone might lag or crash!
 * 
 * LazyVerticalGrid with 100 album cards:
 * - Only creates cards currently visible on screen (~10 cards)
 * - As you scroll, recycles old cards for new items
 * - Uses minimal memory
 * - Smooth scrolling even with 1000+ items!
 * 
 * VISUAL LAYOUT:
 * ┌─────────────────────────────────────────┐
 * │  精选推荐                               │ ← Header
 * │                                         │
 * │  ┌──────┐  ┌──────┐                    │
 * │  │Album1│  │Album2│  ← Row 1           │
 * │  └──────┘  └──────┘                    │
 * │                                         │
 * │  ┌──────┐  ┌──────┐                    │
 * │  │Album3│  │Album4│  ← Row 2           │
 * │  └──────┘  └──────┘                    │
 * │                                         │
 * │  ┌──────┐  ┌──────┐                    │
 * │  │Album5│  │Album6│  ← Row 3           │
 * │  └──────┘  └──────┘                    │
 * │           ... (scroll for more)         │
 * └─────────────────────────────────────────┘
 */
