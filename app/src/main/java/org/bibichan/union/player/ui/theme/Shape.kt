/**
 * Shape.kt - Material 3 形状定义
 *
 * 定义应用的圆角形状，Material 3强调圆角设计。
 * 圆角大小分为：小、中、大、超大四个级别。
 */
package org.bibichan.union.player.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * 应用形状配置
 *
 * 使用较大的圆角以符合Apple Music风格
 */
val Shapes = Shapes(
    // 小圆角：用于小元素（如芯片、标签）
    small = RoundedCornerShape(8.dp),
    
    // 中圆角：用于中等元素（如按钮、卡片）
    medium = RoundedCornerShape(16.dp),
    
    // 大圆角：用于大元素（如底部抽屉、对话框）
    large = RoundedCornerShape(24.dp),
    
    // 超大圆角：用于特殊元素（如全屏卡片）
    extraLarge = RoundedCornerShape(32.dp)
)
