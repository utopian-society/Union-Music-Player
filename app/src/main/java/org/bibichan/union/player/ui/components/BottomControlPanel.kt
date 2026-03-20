/**
 * BottomControlPanel.kt - 底部导航栏
 *
 * 实现Material 3风格的底部导航栏，使用绿色主题
 */
package org.bibichan.union.player.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * 导航项数据类
 */
data class NavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * BottomControlPanel - Material 3 底部导航栏
 *
 * @param items 导航项列表
 * @param selectedIndex 当前选中项索引
 * @param onItemSelected 导航项选中回调
 * @param modifier 修饰符
 */
@Composable
fun BottomControlPanel(
    items: List<NavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // 更亮的绿色配色方案
    val lightGreenColorScheme = NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF1B5E20),      // Dark green for selected icon
        selectedTextColor = Color(0xFF1B5E20),      // Dark green for selected text
        unselectedIconColor = Color(0xFF2E7D32),    // Green 700 for unselected
        unselectedTextColor = Color(0xFF388E3C),    // Green 600 for unselected text
        indicatorColor = Color(0xFFC8E6C9)          // Light green indicator
    )

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        containerColor = Color(0xFF81C784),  // Material Green 300 - much lighter
        contentColor = Color(0xFF1B5E20),
        tonalElevation = 4.dp
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedIndex == index,
                onClick = { onItemSelected(index) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(26.dp)
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = lightGreenColorScheme,
                alwaysShowLabel = true
            )
        }
    }
}

/**
 * 简化版本 - 保持向后兼容
 */
@Composable
fun BottomControlPanel(
    onLibraryClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedIndex: Int = 0
) {
    val navItems = listOf(
        NavItem("library", Icons.Default.LibraryMusic, "Library"),
        NavItem("playlist", Icons.Default.QueueMusic, "Playlist"),
        NavItem("more", Icons.Default.MoreHoriz, "More")
    )

    BottomControlPanel(
        items = navItems,
        selectedIndex = selectedIndex,
        onItemSelected = { index ->
            when (index) {
                0 -> onLibraryClick()
                1 -> onPlaylistClick()
                2 -> onMoreClick()
            }
        },
        modifier = modifier
    )
}
