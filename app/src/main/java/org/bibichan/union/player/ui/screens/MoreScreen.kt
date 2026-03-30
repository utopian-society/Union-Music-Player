package org.bibichan.union.player.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.bibichan.union.player.ui.theme.GreenPrimary
import org.bibichan.union.player.ui.theme.YellowAccent

/**
 * MoreScreen Component
 *
 * The "More" settings screen that displays:
 * - Settings option
 * - Play history option
 * - Other menu items
 *
 * KEY CONCEPTS:
 *
 * 1. LazyColumn:
 *    - Efficient scrollable list (like RecyclerView)
 *    - Only renders visible items
 *    - "Lazy" = loads items on demand
 *
 * 2. ListItem:
 *    - Material Design list item component
 *    - Pre-styled with icon, headline, supporting text
 *    - Automatic ripple effect on touch
 *
 * 3. item vs items:
 *    - item { } = add a single item
 *    - items(count) { } = add multiple items
 *
 * 4. Divider:
 *    - Horizontal line separator
 *    - Visual grouping of list items
 */
@Composable
fun MoreScreen(
    // PARAMETERS:
    // - onSettingsClick: Callback when settings is tapped
    // - onHistoryClick: Callback when play history is tapped
    // - onNavigate: Callback for bottom navigation
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf("More") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // LazyColumn: Scrollable vertical list
        LazyColumn(
            // Add padding around the entire screen
            modifier = Modifier
                .fillMaxSize()      // Fill entire available area
                .padding(bottom = 80.dp) // Space for bottom navigation
        ) {
            // ─────────────────────────────────────────────────────
            // HEADER: "更多" (More)
            // ─────────────────────────────────────────────────────
            item {  // item { } adds a single composable to the list
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "更多",  // Chinese for "More"
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // ─────────────────────────────────────────────────────
            // SETTINGS LIST ITEM
            // ─────────────────────────────────────────────────────
            item {
                ListItem(
                    // headlineContent: Main text (required)
                    headlineContent = {
                        Text("设置")  // Chinese for "Settings"
                    },

                    // leadingContent: Icon before the text (optional)
                    leadingContent = {
                        Icon(
                            // Settings icon looks like ⚙️ (gear)
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            // Green color for this option
                            tint = GreenPrimary
                        )
                    },

                    // trailingContent: Icon/text after the main content (optional)
                    trailingContent = {
                        // Chevron icon indicates this item is clickable
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Go to settings",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },

                    // modifier: Make the entire row clickable
                    modifier = Modifier
                )

                // Divider: Horizontal line after this item
                HorizontalDivider()
            }

            // ─────────────────────────────────────────────────────
            // PLAY HISTORY LIST ITEM
            // ─────────────────────────────────────────────────────
            item {
                ListItem(
                    headlineContent = {
                        Text("播放历史")  // Chinese for "Play History"
                    },
                    leadingContent = {
                        Icon(
                            // History icon looks like 🕐 (clock)
                            imageVector = Icons.Default.History,
                            contentDescription = "History",
                            // Yellow accent for this option
                            tint = YellowAccent
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View history",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                HorizontalDivider()
            }

            // ─────────────────────────────────────────────────────
            // ABOUT LIST ITEM (additional example)
            // ─────────────────────────────────────────────────────
            item {
                ListItem(
                    headlineContent = {
                        Text("关于")  // Chinese for "About"
                    },
                    leadingContent = {
                        Icon(
                            // Info icon looks like ℹ️
                            imageVector = Icons.Default.Info,
                            contentDescription = "About",
                            // Blue color for info
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View about",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )

                HorizontalDivider()
            }

            // ─────────────────────────────────────────────────────
            // HELP LIST ITEM (additional example)
            // ─────────────────────────────────────────────────────
            item {
                ListItem(
                    headlineContent = {
                        Text("帮助")  // Chinese for "Help"
                    },
                    leadingContent = {
                        Icon(
                            // Help icon looks like ❓ (question mark)
                            // Using AutoMirrored version as Icons.Default.Help is deprecated
                            imageVector = Icons.AutoMirrored.Filled.Help,
                            contentDescription = "Help",
                            // Purple color for help
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    },
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View help",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // Fixed Bottom Navigation Bar with blur effect
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
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
                        onClick = {
                            selectedTab = label
                            onNavigate(label.lowercase())
                        },
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
}

/**
 * HOW LAZYCOLUMN WORKS:
 *
 * Regular Column with 100 list items:
 * - Creates ALL 100 items at once
 * - Wastes memory on items user can't see
 *
 * LazyColumn with 100 list items:
 * - Only creates items visible on screen (~10 items)
 * - As you scroll, recycles old items for new ones
 * - Much more efficient!
 *
 * VISUAL LAYOUT:
 * ┌─────────────────────────────────────────┐
 * │  更多                                   │ ← Header
 * │                                         │
 * │  ⚙️  设置                    ›          │ ← ListItem 1
 * │  ─────────────────────────────          │ ← Divider
 * │  🕐  播放历史                ›          │ ← ListItem 2
 * │  ─────────────────────────────          │ ← Divider
 * │  ℹ️  关于                    ›          │ ← ListItem 3
 * │  ─────────────────────────────          │ ← Divider
 * │  ❓  帮助                    ›          │ ← ListItem 4
 * │           ... (scroll for more)         │
 * ├─────────────────────────────────────────┤
 * │  ┌─────────────────────────────────┐    │
 * │  │  📚 Library    ⋮ More           │    │ ← Bottom Nav
 * │  └─────────────────────────────────┘    │
 * └─────────────────────────────────────────┘
 */
