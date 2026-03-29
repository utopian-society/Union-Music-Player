package org.bibichan.union.player.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Union Music Player Color Palette
 * 
 * This file defines all the colors used throughout the app.
 * Colors follow Material 3 design guidelines.
 */

// ─────────────────────────────────────────────────────────────
// PRIMARY COLORS
// ─────────────────────────────────────────────────────────────

/**
 * Main brand color - used for:
 * - TopAppBar background
 * - Primary action buttons
 * - Selected "Library" tab icon
 * 
 * Color value: #4CAF50 (Material Design Green 500)
 * 0xFF = Fully opaque (no transparency)
 * 4C = Red component
 * AF = Green component  
 * 50 = Blue component
 */
val GreenPrimary = Color(0xFF4CAF50)

/**
 * Secondary green - lighter shade for:
 * - Less prominent elements
 * - Alternative buttons
 * - Dark theme primary color
 * 
 * Color value: #8BC34A (Material Design Green 300)
 */
val GreenSecondary = Color(0xFF8BC34A)

// ─────────────────────────────────────────────────────────────
// ACCENT COLORS
// ─────────────────────────────────────────────────────────────

/**
 * Yellow accent color - used for:
 * - Play/Pause button in MiniPlayer
 * - Selected "More" tab icon
 * - Special highlights
 * 
 * Color value: #FFC107 (Material Design Amber 500)
 */
val YellowAccent = Color(0xFFFFC107)

// ─────────────────────────────────────────────────────────────
// BACKGROUND COLORS
// ─────────────────────────────────────────────────────────────

/**
 * Translucent white - creates glass-morphism effect
 * Used for:
 * - MiniPlayer background (semi-transparent)
 * - Bottom navigation background
 * 
 * 0x80 = 50% transparency (allows content behind to show through)
 * FF = Red (full)
 * FF = Green (full)
 * FF = Blue (full)
 * Result: Semi-transparent white
 */
val TranslucentWhite = Color(0x80FFFFFF)

/**
 * Dark background for dark theme support
 * Color value: #121212 (Material Design dark surface)
 */
val DarkBackground = Color(0xFF121212)

/**
 * Light background for light theme
 * Color value: #FAFAFA (Almost white, slightly warm)
 */
val LightBackground = Color(0xFFFAFAFA)
