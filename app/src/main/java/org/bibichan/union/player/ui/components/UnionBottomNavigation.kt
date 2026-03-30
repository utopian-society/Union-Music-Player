package org.bibichan.union.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MoreVert
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
 * UnionBottomNavigation Component - Apple Music Style
 *
 * The bottom navigation bar that allows switching between:
 * - Library (资料库) - Browse music collection
 * - More (更多) - Settings and options
 *
 * Visual Properties:
 * - 12dp corner radius
 * - BackdropFilter blur effect (sigmaX=8f, sigmaY=8f)
 * - Semi-transparent white background (90% opacity)
 * - Fixed position at screen bottom
 *
 * KEY CONCEPTS:
 *
 * 1. NavigationBar:
 *    - Material 3 bottom navigation container
 *    - Holds NavigationBarItem components
 *    - Automatically handles layout and spacing
 *
 * 2. NavigationBarItem:
 *    - Individual navigation button
 *    - Shows icon + label
 *    - Has selected/unselected states
 *
 * 3. State management:
 *    - currentRoute: Which screen is currently visible
 *    - onNavigate: Callback to change the current screen
 *
 * 4. Selected vs Unselected:
 *    - Selected tab: Uses selectedIconColor and selectedTextColor
 *    - Unselected tab: Uses default colors (gray)
 *    - Indicator: Pill-shaped background behind selected item
 */
@Composable
fun UnionBottomNavigation(
    // PARAMETERS:
    // - currentRoute: String identifying current screen ("library" or "more")
    // - onNavigate: Callback to navigate to a different screen
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val selectedTabState = remember {
        mutableStateOf(if (currentRoute == "library") "Library" else "More")
    }

    // Update selectedTab when currentRoute changes from outside
    LaunchedEffect(currentRoute) {
        selectedTabState.value = if (currentRoute == "library") "Library" else "More"
    }

    // NavigationBar: Container for bottom navigation items
    Box(
        modifier = Modifier
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
            // ─────────────────────────────────────────────────────
            // LIBRARY TAB (资料库)
            // ─────────────────────────────────────────────────────
            NavigationBarItem(
                // selected: Is this tab currently active?
                // Compares currentRoute with "library"
                // Returns true if matching, false otherwise
                selected = selectedTabState.value == "Library",

                // onClick: Called when user taps this tab
                onClick = {
                    selectedTabState.value = "Library"
                    onNavigate("library")  // Tell parent to show library
                },

                // icon: What icon to display
                icon = {
                    Icon(
                        // LibraryBooks icon looks like 📚 (books on shelf)
                        imageVector = Icons.Default.LibraryBooks,
                        contentDescription = "Library"  // For accessibility
                    )
                },

                // label: Text shown below the icon
                label = {
                    Text(
                        text = "Library",
                        fontSize = 10.sp,
                        fontWeight = if (selectedTabState.value == "Library") FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTabState.value == "Library") Color.Black else Color(0xFF666666),
                        maxLines = 1
                    )
                },

                // colors: Customize appearance
                colors = NavigationBarItemDefaults.colors(
                    // selectedIconColor: Icon color when this tab is active
                    selectedIconColor = Color.Black,

                    // selectedTextColor: Text color when active
                    selectedTextColor = Color.Black,

                    // indicatorColor: Pill background color behind selected item
                    indicatorColor = Color(0xFFE5E5E5),

                    // unselected colors
                    unselectedIconColor = Color(0xFF666666),
                    unselectedTextColor = Color(0xFF666666)
                )
            )

            // ─────────────────────────────────────────────────────
            // MORE TAB (更多)
            // ─────────────────────────────────────────────────────
            NavigationBarItem(
                // Check if current screen is "more"
                selected = selectedTabState.value == "More",

                // Navigate to more screen when tapped
                onClick = {
                    selectedTabState.value = "More"
                    onNavigate("more")
                },

                // Icon: MoreVert looks like ⋮ (three vertical dots)
                icon = {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More"
                    )
                },

                // Label text
                label = {
                    Text(
                        text = "More",
                        fontSize = 10.sp,
                        fontWeight = if (selectedTabState.value == "More") FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedTabState.value == "More") Color.Black else Color(0xFF666666),
                        maxLines = 1
                    )
                },

                // Different colors for variety
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = Color.Black,
                    indicatorColor = Color(0xFFE5E5E5),
                    unselectedIconColor = Color(0xFF666666),
                    unselectedTextColor = Color(0xFF666666)
                )
            )
        }
    }
}

/**
 * HOW NAVIGATION WORKS:
 *
 * 1. Parent component (UnionMusicApp) holds the state:
 *    ```
 *    var currentRoute by remember { mutableStateOf("library") }
 *    ```
 *
 * 2. Pass state to NavigationBar:
 *    ```
 *    UnionBottomNavigation(
 *        currentRoute = currentRoute,
 *        onNavigate = { newRoute -> currentRoute = newRoute }
 *    )
 *    ```
 *
 * 3. User taps "More" tab:
 *    - onClick = { onNavigate("more") } is called
 *    - Parent updates currentRoute to "more"
 *    - UI rebuilds with new selected tab
 *    - Screen content changes to MoreScreen
 *
 * VISUAL RESULT:
 * ┌─────────────────────────────────────────┐
 * │  ┌───────────────────────────────────┐  │
 * │  │  📚 Library    ⋮ More             │  │
 * │  │  ←──── Selected ────→             │  │
 * │  │  (black color + indicator pill)   │  │
 * │  └───────────────────────────────────┘  │
 * └─────────────────────────────────────────┘
 */
