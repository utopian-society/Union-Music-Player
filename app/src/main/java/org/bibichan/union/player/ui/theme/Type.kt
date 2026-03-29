package org.bibichan.union.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Union Music Player Typography
 * 
 * This file defines text styles used throughout the app.
 * Material 3 provides predefined styles like titleLarge, bodyMedium, etc.
 * 
 * What is "sp"?
 * - "sp" = "scale-independent pixels"
 * - Like "dp" but scales with user's font size preference
 * - Always use "sp" for text (accessibility best practice)
 */

/**
 * Typography defines text styles for different use cases.
 * 
 * Material 3 provides these predefined styles:
 * - displayLarge/medium/small  → Very large headings
 * - headlineLarge/medium/small → Section titles
 * - titleLarge/medium/small    → Card titles, dialog titles
 * - bodyLarge/medium/small     → Paragraph text, descriptions
 * - labelLarge/medium/small    → Button text, tab labels
 */
val Typography = Typography(
    
    /**
     * titleLarge: Used for:
     * - Album titles
     * - Page headers
     * 
     * Properties:
     * - fontWeight = Bold (makes text thicker, more prominent)
     * - fontSize = 22sp (large enough to stand out)
     * - lineHeight = 28sp (space between lines for readability)
     */
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,  // Uses system default font
        fontWeight = FontWeight.Bold,      // Bold weight for emphasis
        fontSize = 22.sp,                  // 22 scale-independent pixels
        lineHeight = 28.sp,                // Line height for spacing
        letterSpacing = 0.sp               // No extra space between letters
    ),
    
    /**
     * titleMedium: Used for:
     * - Album card titles
     * - Song titles in lists
     */
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,  // Slightly less bold than titleLarge
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp            // Tiny letter spacing for style
    ),
    
    /**
     * bodyLarge: Used for:
     * - Song title in MiniPlayer
     * - Main content text
     */
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,    // Regular weight for readability
        fontSize = 16.sp,
        lineHeight = 24.sp,                // Comfortable line spacing
        letterSpacing = 0.5.sp             // Slight letter spacing
    ),
    
    /**
     * bodySmall: Used for:
     * - Artist name under album title
     * - Secondary/description text
     * - Subtitles
     */
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,                  // Smaller than bodyLarge
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    
    /**
     * labelSmall: Used for:
     * - Bottom navigation labels
     * - Small button text
     */
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,    // Medium weight for small text
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
