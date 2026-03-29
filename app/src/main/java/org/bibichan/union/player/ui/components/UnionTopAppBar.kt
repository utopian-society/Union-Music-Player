package org.bibichan.union.player.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.bibichan.union.player.ui.theme.GreenPrimary

/**
 * UnionTopAppBar Component
 * 
 * The top application bar (header) that displays:
 * - App title with music emoji
 * - Green background (brand color)
 * 
 * KEY CONCEPTS:
 * 
 * 1. TopAppBar:
 *    - Standard Material Design app bar component
 *    - Provides consistent header across Android apps
 *    - Supports title, navigation icon, and action buttons
 * 
 * 2. TopAppBarDefaults:
 *    - Pre-configured colors for TopAppBar
 *    - Ensures proper contrast and accessibility
 * 
 * 3. Lambda syntax: { }
 *    - title = { Text("...") } means "a function that returns Text"
 *    - TopAppBar calls this lambda to get the title content
 */
@Composable
fun UnionTopAppBar() {
    // TopAppBar: Standard Android app header
    TopAppBar(
        // title: What to display as the title
        // The { } creates a lambda (function) that returns the Text composable
        title = {
            Text(
                text = "🤖 Union Music Player",  // App title with music emoji
                // You could also style the text:
                // style = MaterialTheme.typography.titleLarge,
                // color = Color.White
            )
        },
        
        // colors: Customize the app bar appearance
        colors = TopAppBarDefaults.topAppBarColors(
            // containerColor: Background color of the app bar
            containerColor = GreenPrimary,  // Our brand green
            
            // titleContentColor: Color of the title text
            titleContentColor = Color.White,  // White on green = good contrast
            
            // navigationIconContentColor: Color of hamburger menu (if added)
            navigationIconContentColor = Color.White,
            
            // actionIconContentColor: Color of action buttons (if added)
            actionIconContentColor = Color.White
        )
        
        // NOTE: We're NOT adding any action buttons
        // The design spec said to remove the account icon
        // 
        // If you wanted to add actions, you would use:
        // actions = {
        //     IconButton(onClick = { /* do something */ }) {
        //         Icon(Icons.Default.Search, "Search")
        //     }
        // }
    )
}

/**
 * HOW TOPAPPBAR WORKS INTERNALLY:
 * 
 * TopAppBar is essentially a Box with:
 * - Fixed height (64dp by default)
 * - Background color (containerColor)
 * - Padding for content
 * - Slots for: navigation icon, title, actions
 * 
 * LAYOUT STRUCTURE:
 * ┌─────────────────────────────────────────┐
 * │ [Nav]  Title            [Action1][A2]  │ ← TopAppBar
 * │  icon                              icons │
 * └─────────────────────────────────────────┘
 * 
 * In our case:
 * ┌─────────────────────────────────────────┐
 * │        🎵 音乐播放器                    │ ← No nav, no actions
 * └─────────────────────────────────────────┘
 */
