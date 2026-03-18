/**
 * Color.kt - Material 3 颜色定义
 *
 * 定义应用的主题颜色，使用绿色、黄色和白色作为主要颜色。
 * Material 3 使用色调调色板系统，为不同的UI元素提供不同的颜色变体。
 */
package org.bibichan.union.player.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== 主色调 - 绿色系 ====================
/**
 * 主色调：绿色
 * 用于主要按钮、重要元素等
 */
val GreenPrimary = Color(0xFF4CAF50)      // Material Green 500
val GreenLight = Color(0xFF81C784)        // Material Green 300
val GreenDark = Color(0xFF388E3C)          // Material Green 700
val GreenMuted = Color(0xFFC8E6C9)         // Material Green 100

// ==================== 次要色调 - 黄色系 ====================
/**
 * 次要色调：黄色
 * 用于次要按钮、强调元素等
 */
val YellowPrimary = Color(0xFFFFC107)      // Material Amber 500
val YellowLight = Color(0xFFFFD54F)        // Material Amber 300
val YellowDark = Color(0xFFFF8F00)         // Material Amber 700
val YellowMuted = Color(0xFFFFECB3)        // Material Amber 100

// ==================== 背景色 - 白色系 ====================
/**
 * 背景色：白色系
 * 用于界面背景、卡片等
 */
val White = Color(0xFFFFFFFF)
val WhiteSmoke = Color(0xFFF8F8F8)
val OffWhite = Color(0xFFF5F5F5)
val LightGray = Color(0xFFE0E0E0)

// ==================== 文字颜色 ====================
/**
 * 文字颜色
 */
val TextPrimary = Color(0xFF212121)        // 深灰色，用于主要文字
val TextSecondary = Color(0xFF757575)      // 中灰色，用于次要文字
val TextOnPrimary = Color(0xFFFFFFFF)      // 白色，用于主色调背景上的文字
val TextOnSecondary = Color(0xFF212121)    // 深色，用于次要色调背景上的文字

// ==================== 其他颜色 ====================
/**
 * 功能性颜色
 */
val ErrorRed = Color(0xFFE53935)           // 错误提示色
val SuccessGreen = Color(0xFF43A047)       // 成功提示色
val InfoBlue = Color(0xFF1E88E5)           // 信息提示色
val WarningYellow = Color(0xFFFFB300)      // 警告提示色

// ==================== 暗色主题颜色 ====================
/**
 * 暗色主题专用颜色
 */
val DarkBackground = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkPrimary = Color(0xFF66BB6A)        // 暗色主题下的主色调
val DarkSecondary = Color(0xFFFFCA28)      // 暗色主题下的次要色调
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFB0B0B0)
