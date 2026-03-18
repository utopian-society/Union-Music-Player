/**
 * Theme.kt - Material 3 主题配置
 *
 * 定义应用的Material 3主题，包括颜色方案、形状等。
 * Material 3提供了更现代、更灵活的设计系统。
 */
package org.bibichan.union.player.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ==================== 亮色主题 ====================
/**
 * 亮色主题颜色方案
 * 使用绿色、黄色和白色作为主要颜色
 */
private val LightColorScheme = lightColorScheme(
    // 主色调 - 绿色系
    primary = GreenPrimary,
    onPrimary = TextOnPrimary,
    primaryContainer = GreenMuted,
    onPrimaryContainer = TextPrimary,
    
    // 次要色调 - 黄色系
    secondary = YellowPrimary,
    onSecondary = TextOnSecondary,
    secondaryContainer = YellowMuted,
    onSecondaryContainer = TextPrimary,
    
    // 第三色调
    tertiary = GreenDark,
    onTertiary = TextOnPrimary,
    tertiaryContainer = GreenLight,
    onTertiaryContainer = TextPrimary,
    
    // 背景色
    background = White,
    onBackground = TextPrimary,
    
    // 表面色（卡片、菜单等）
    surface = White,
    onSurface = TextPrimary,
    surfaceVariant = OffWhite,
    onSurfaceVariant = TextSecondary,
    
    // 轮廓色（边框、分隔线等）
    outline = LightGray,
    outlineVariant = Color(0xFFE0E0E0),
    
    // 错误色
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF410002),
    
    // 反色（用于反向强调）
    inverseSurface = Color(0xFF313131),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = GreenLight,
    
    // 其他
    scrim = Color.Black
)

// ==================== 暗色主题 ====================
/**
 * 暗色主题颜色方案
 */
private val DarkColorScheme = darkColorScheme(
    // 主色调 - 绿色系
    primary = DarkPrimary,
    onPrimary = Color(0xFF003A12),
    primaryContainer = GreenDark,
    onPrimaryContainer = GreenMuted,
    
    // 次要色调 - 黄色系
    secondary = DarkSecondary,
    onSecondary = Color(0xFF3E2E00),
    secondaryContainer = Color(0xFF573E00),
    onSecondaryContainer = YellowMuted,
    
    // 第三色调
    tertiary = GreenLight,
    onTertiary = Color(0xFF003A12),
    tertiaryContainer = GreenDark,
    onTertiaryContainer = GreenMuted,
    
    // 背景色
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    
    // 表面色
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = DarkTextSecondary,
    
    // 轮廓色
    outline = Color(0xFF8E918F),
    outlineVariant = Color(0xFF444744),
    
    // 错误色
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    
    // 反色
    inverseSurface = Color(0xFFE0E0E0),
    inverseOnSurface = Color(0xFF1C1C1C),
    inversePrimary = GreenPrimary,
    
    // 其他
    scrim = Color.Black
)

/**
 * UnionMusicPlayerTheme - 应用主题
 *
 * @param darkTheme 是否使用暗色主题，默认跟随系统
 * @param dynamicColor 是否使用动态颜色（Android 12+），默认关闭以保持品牌色
 * @param content 主题内容
 */
@Composable
fun UnionMusicPlayerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 默认关闭动态颜色，以保持绿色+黄色+白色的品牌色
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // 如果启用动态颜色且系统版本支持（Android 12+）
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 否则使用我们定义的品牌色
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
