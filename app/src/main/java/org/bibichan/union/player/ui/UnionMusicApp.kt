/**
 * UnionMusicApp.kt - 主应用Composable
 *
 * 这是应用的主Composable，包含浮动播放器和底部三按钮控制面板。
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
import org.bibichan.union.player.ui.components.BottomControlPanel
import org.bibichan.union.player.ui.components.FloatingPlayer
import org.bibichan.union.player.ui.components.PlaylistSelector
import org.bibichan.union.player.ui.screens.*
import java.io.File

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
    var isPlayerExpanded by remember { mutableStateOf(false) }
    var showPlaylistSelector by remember { mutableStateOf(false) }
    
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
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 主要内容区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp) // 为底部控制面板预留空间
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
        
        // 浮动播放器
        AnimatedVisibility(
            visible = musicPlayer.getCurrentSong() != null,
            enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it }
            ) + androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it }
            ) + androidx.compose.animation.fadeOut()
        ) {
            FloatingPlayer(
                musicPlayer = musicPlayer,
                isVisible = isPlayerExpanded,
                onExpand = { isPlayerExpanded = true },
                onCollapse = { isPlayerExpanded = false }
            )
        }
        
        // 底部控制面板
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            BottomControlPanel(
                onLibraryClick = { selectedTab = 0 },
                onPlaylistClick = { showPlaylistSelector = true },
                onMoreClick = { selectedTab = 2 }
            )
        }
        
        // 播放列表选择器
        if (showPlaylistSelector) {
            PlaylistSelector(
                onPlaylistSelected = { uri ->
                    // 处理选中的播放列表文件
                    // 这里应该解析m3u8文件并加载到播放器中
                },
                onDismiss = { showPlaylistSelector = false }
            )
        }
    }
}
