package org.bibichan.union.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.bibichan.union.player.data.Song
import org.bibichan.union.player.ui.theme.GreenPrimary
import org.bibichan.union.player.ui.theme.YellowAccent

/**
 * MiniPlayer Component
 * 
 * This is the bottom mini-player bar that shows:
 * - Current song title and artist
 * - Play/Pause/Previous/Next control buttons
 * 
 * KEY CONCEPTS:
 * 
 * 1. @Composable:
 *    - Marks this function as a UI builder
 *    - Can be called from other @Composable functions
 *    - Automatically updates UI when data changes
 * 
 * 2. Modifier:
 *    - How Compose styles and layouts components
 *    - Chain multiple modifiers together
 *    - Examples: .padding(), .fillMaxWidth(), .background()
 * 
 * 3. Lambda callbacks:
 *    - onPlayPause: () -> Unit means "a function with no parameters, returns nothing"
 *    - Parent component provides these callbacks
 *    - MiniPlayer calls them when buttons are clicked
 * 
 * 4. Row and Column:
 *    - Row: Arranges children horizontally (left to right)
 *    - Column: Arranges children vertically (top to bottom)
 */
@Composable
fun MiniPlayer(
    // PARAMETERS:
    // - currentSong: The song currently loaded
    // - isPlaying: Whether music is playing (true) or paused (false)
    // - onPlayPause: Callback when play/pause button is tapped
    // - onPrevious: Callback when previous button is tapped
    // - onNext: Callback when next button is tapped
    // - modifier: Optional modifier for external styling
    currentSong: Song,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Box: Stacks children on top of each other (like a frame container)
    Box(
        modifier = modifier
            // Fill entire width of parent
            .fillMaxWidth()
            // Fixed height of 70 density-independent pixels
            .height(70.dp)
            // Gradient background (top to bottom)
            .background(
                Brush.verticalGradient(
                    // Gradient colors: white with slight transparency change
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),  // 95% opaque white
                        Color.White.copy(alpha = 0.9f)    // 90% opaque white
                    )
                )
            )
            // Add horizontal padding (16dp on left and right)
            .padding(horizontal = 16.dp)
    ) {
        // Row: Arrange song info and buttons horizontally
        Row(
            modifier = Modifier.fillMaxSize(),  // Fill the Box
            verticalAlignment = Alignment.CenterVertically  // Center items vertically
        ) {
            // Column: Stack song title and artist vertically
            Column(
                // weight(1f) means "take all remaining horizontal space"
                // This pushes the buttons to the right side
                modifier = Modifier.weight(1f)
            ) {
                // Song title text
                Text(
                    text = currentSong.title,  // Display song's title
                    style = MaterialTheme.typography.bodyLarge,  // Use theme's text style
                    maxLines = 1,  // Only show 1 line (no wrapping)
                    color = Color.Black  // Black text on white background
                )
                
                // Artist name text
                Text(
                    text = currentSong.artist,  // Display song's artist
                    style = MaterialTheme.typography.bodySmall,  // Smaller text style
                    maxLines = 1,  // Only show 1 line
                    color = Color.Gray  // Gray color for secondary info
                )
            }
            
            // Row: Arrange control buttons horizontally
            Row(
                // Add 8dp space between each button
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                // Align buttons vertically with song info
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ─────────────────────────────────────────────────────
                // PREVIOUS BUTTON
                // ─────────────────────────────────────────────────────
                IconButton(
                    onClick = onPrevious  // Call parent's callback when tapped
                ) {
                    Icon(
                        // SkipPrevious icon (looks like |◀◀)
                        imageVector = Icons.Default.SkipPrevious,
                        // Accessibility description (for screen readers)
                        contentDescription = "Previous",
                        // Green color for secondary action
                        tint = GreenPrimary,
                        // Make icon 32dp in size
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // ─────────────────────────────────────────────────────
                // PLAY/PAUSE BUTTON (larger, more prominent)
                // ─────────────────────────────────────────────────────
                IconButton(
                    onClick = onPlayPause,  // Call parent's callback
                    // Make this button larger (40dp) for emphasis
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        // DYNAMIC ICON: Changes based on isPlaying state
                        // - If playing: show Pause icon (⏸️)
                        // - If paused: show Play icon (▶️)
                        imageVector = if (isPlaying) {
                            Icons.Default.Pause
                        } else {
                            Icons.Default.PlayArrow
                        },
                        // Accessibility: describe current action
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        // Yellow accent color for primary action
                        tint = YellowAccent
                    )
                }
                
                // ─────────────────────────────────────────────────────
                // NEXT BUTTON
                // ─────────────────────────────────────────────────────
                IconButton(
                    onClick = onNext  // Call parent's callback when tapped
                ) {
                    Icon(
                        // SkipNext icon (looks like ▶▶|)
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = GreenPrimary,  // Green for secondary action
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}
