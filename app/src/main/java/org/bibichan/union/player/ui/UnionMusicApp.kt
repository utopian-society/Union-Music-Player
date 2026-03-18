/**
 * UnionMusicApp.kt - 主应用Composable
 *
 * 这是应用的主Composable，包含底部三按钮导航和主要界面。
 * 设计参考Apple Music风格：Library、Playlist、More三个主要功能。
 */
package org.bibichan.union.player.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.ui.screens.*

/**
 * 导航项数据类
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val title: String
)

/**
 * UnionMusicApp - 主应用Composable
 *
 * @param musicPlayer 音乐播放器实例
 * @param onRequestPermission 请求权限的回调
 */
@Composable
fun UnionMusicApp(
    musicPlayer: MusicPlayer,
    onRequestPermission: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    // 定义底部导航项
    val navItems = listOf(
        BottomNavItem(
            route = "library",
            icon = Icons.Default.LibraryMusic,
            title = "Library"
        ),
        BottomNavItem(
            route = "playlist",
            icon = Icons.Default.QueueMusic,
            title = "Playlist"
        ),
        BottomNavItem(
            route = "more",
            icon = Icons.Default.MoreHoriz,
            title = "More"
        )
    )
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .height(80.dp)
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(28.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> LibraryScreen(
                    musicPlayer = musicPlayer,
                    onRequestPermission = onRequestPermission
                )
                1 -> PlaylistScreen(
                    musicPlayer = musicPlayer
                )
                2 -> MoreScreen(
                    onRequestPermission = onRequestPermission
                )
            }
        }
    }
}
