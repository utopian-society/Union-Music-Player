package org.bibichan.union.player.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Union Music Player Theme
 * 
 * This file defines the app's theme system.
 * The theme provides colors and typography to all child components.
 * 
 * KEY CONCEPTS:
 * 
 * 1. ColorScheme:
 *    - Bundles all colors used in Material Design
 *    - Has semantic names (primary, surface, onPrimary, etc.)
 *    - "onPrimary" = color of text ON primary background
 * 
 * 2. lightColorScheme vs darkColorScheme:
 *    - lightColorScheme: Colors when phone is in light mode
 *    - darkColorScheme: Colors when phone is in dark mode
 * 
 * 3. @Composable:
 *    - Annotation that marks a function as "composable"
 *    - Composable functions can build UI
 *    - Can only be called from other composable functions
 */

// ─────────────────────────────────────────────────────────────
// LIGHT THEME COLOR SCHEME
// ─────────────────────────────────────────────────────────────

/**
 * LightColorScheme defines colors for light theme.
 * 
 * Material 3 ColorScheme properties:
 * - primary: Main brand color (our green)
 * - secondary: Supporting color (lighter green)
 * - tertiary: Accent color (yellow)
 * - surface: Background for cards, sheets
 * - background: Main app background
 * - onPrimary: Text color on primary background (white on green)
 * - onSurface: Text color on surface background (black on white)
 */
private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,           // TopAppBar, primary buttons
    secondary = GreenSecondary,       // Secondary elements
    tertiary = YellowAccent,          // Accent elements
    surface = TranslucentWhite,       // Card backgrounds (semi-transparent)
    background = LightBackground,     // Main app background
    onPrimary = Color.White,          // Text on green background
    onSecondary = Color.Black,        // Text on light green
    onTertiary = Color.Black,         // Text on yellow
    onSurface = Color.Black,          // Text on white/surface
    onBackground = Color.Black        // Text on main background
)

// ─────────────────────────────────────────────────────────────
// DARK THEME COLOR SCHEME
// ─────────────────────────────────────────────────────────────

/**
 * DarkColorScheme defines colors for dark theme.
 * 
 * Dark theme uses different colors for better contrast:
 * - Darker backgrounds
 * - Lighter text
 * - Adjusted primary colors (less vibrant for dark mode)
 */
private val DarkColorScheme = darkColorScheme(
    primary = GreenSecondary,         // Slightly different green for dark mode
    secondary = GreenPrimary,
    tertiary = YellowAccent,
    surface = Color(0xFF1E1E1E),      // Dark gray surface
    background = DarkBackground,      // Very dark background
    onPrimary = Color.Black,          // Dark text on green
    onSecondary = Color.White,        // White text on green
    onTertiary = Color.Black,
    onSurface = Color.White,          // White text on dark surface
    onBackground = Color.White        // White text on dark background
)

// ─────────────────────────────────────────────────────────────
// MAIN THEME COMPOSABLE
// ─────────────────────────────────────────────────────────────

/**
 * MusicTheme is the main theme wrapper for the app.
 * 
 * PARAMETERS:
 * - darkTheme: Boolean - whether to use dark theme
 *   - Defaults to isSystemInDarkTheme() (follows phone setting)
 * - content: The UI content to wrap with the theme
 * 
 * HOW IT WORKS:
 * 1. Checks if dark theme is enabled
 * 2. Selects appropriate color scheme
 * 3. Updates system status bar color
 * 4. Wraps content with MaterialTheme
 * 
 * USAGE:
 * ```
 * MusicTheme {
 *     // All UI components here inherit the theme
 *     Text("Hello", color = MaterialTheme.colorScheme.onSurface)
 * }
 * ```
 */
@Composable
fun MusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),  // Auto-detect system theme
    content: @Composable () -> Unit              // UI content to wrap
) {
    // Choose color scheme based on theme preference
    val colorScheme = if (darkTheme) {
        DarkColorScheme   // Use dark colors
    } else {
        LightColorScheme  // Use light colors
    }
    
    // Get the current Android View (needed for status bar modification)
    val view = LocalView.current
    
    // SideEffect: Runs code outside of Compose's normal rendering
    // This is needed because status bar is part of Android system, not Compose
    if (!view.isInEditMode) {  // Skip in preview editor
        SideEffect {
            // Get the Android window
            val window = (view.context as Activity).window
            
            // Set status bar color to match our primary color
            // toArgb() converts Compose Color to Android color int
            window.statusBarColor = colorScheme.primary.toArgb()
            
            // Set status bar icon appearance
            // isAppearanceLightStatusBars = true → Dark icons (for light theme)
            // isAppearanceLightStatusBars = false → Light icons (for dark theme)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    
    // MaterialTheme provides colors and typography to all child components
    MaterialTheme(
        colorScheme = colorScheme,  // Apply our color scheme
        typography = Typography,    // Apply our text styles
        content = content           // The UI content wrapped in theme
    )
}
