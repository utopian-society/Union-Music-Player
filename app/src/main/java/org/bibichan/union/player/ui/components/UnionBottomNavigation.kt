package org.bibichan.union.player.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.bibichan.union.player.ui.theme.GreenPrimary
import org.bibichan.union.player.ui.theme.YellowAccent

/**
 * UnionBottomNavigation Component
 * 
 * The bottom navigation bar that allows switching between:
 * - Library (资料库) - Browse music collection
 * - More (更多) - Settings and options
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
    // NavigationBar: Container for bottom navigation items
    NavigationBar(
        // containerColor: Background color of the navigation bar
        containerColor = Color.White.copy(alpha = 0.9f),  // Semi-transparent white
        
        // tonalElevation: Creates subtle shadow effect
        // Higher elevation = more shadow = appears "above" content
        tonalElevation = 8.dp
    ) {
        // ─────────────────────────────────────────────────────
        // LIBRARY TAB (资料库)
        // ─────────────────────────────────────────────────────
        NavigationBarItem(
            // selected: Is this tab currently active?
            // Compares currentRoute with "library"
            // Returns true if matching, false otherwise
            selected = currentRoute == "library",
            
            // onClick: Called when user taps this tab
            onClick = { onNavigate("library") },  // Tell parent to show library
            
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
                Text("资料库")  // Chinese for "Library"
            },
            
            // colors: Customize appearance
            colors = NavigationBarItemDefaults.colors(
                // selectedIconColor: Icon color when this tab is active
                selectedIconColor = GreenPrimary,  // Green for library
                
                // selectedTextColor: Text color when active
                selectedTextColor = GreenPrimary,
                
                // indicatorColor: Pill background color behind selected item
                // copy(alpha = 0.12f) = 12% opacity = very faint
                indicatorColor = GreenPrimary.copy(alpha = 0.12f)
            )
        )
        
        // ─────────────────────────────────────────────────────
        // MORE TAB (更多)
        // ─────────────────────────────────────────────────────
        NavigationBarItem(
            // Check if current screen is "more"
            selected = currentRoute == "more",
            
            // Navigate to more screen when tapped
            onClick = { onNavigate("more") },
            
            // Icon: MoreVert looks like ⋮ (three vertical dots)
            icon = {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More"
                )
            },
            
            // Label text
            label = {
                Text("更多")  // Chinese for "More"
            },
            
            // Different colors for variety (yellow accent)
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = YellowAccent,  // Yellow for more tab
                selectedTextColor = YellowAccent,
                indicatorColor = YellowAccent.copy(alpha = 0.12f)
            )
        )
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
 * │                                         │
 * │     📚 资料库          ⋮ 更多           │
 * │     ←──── Selected ────→                │
 * │     (green color + indicator pill)      │
 * └─────────────────────────────────────────┘
 */
