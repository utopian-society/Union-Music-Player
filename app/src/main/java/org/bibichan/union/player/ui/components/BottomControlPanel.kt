/**
 * BottomControlPanel.kt - 底部控制面板
 *
 * 实现Apple Music风格的底部三按钮控制面板
 */
package org.bibichan.union.player.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

@Composable
fun BottomControlPanel(
    onLibraryClick: () -> Unit,
    onPlaylistClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .zIndex(1f)
    ) {
        // 左侧按钮 - Library
        FloatingActionButton(
            onClick = onLibraryClick,
            modifier = Modifier.align(Alignment.CenterStart),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LibraryMusic,
                contentDescription = "Library",
                modifier = Modifier.size(24.dp)
            )
        }
        
        // 中间按钮 - Playlist
        FloatingActionButton(
            onClick = onPlaylistClick,
            modifier = Modifier.align(Alignment.Center),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "Playlist",
                modifier = Modifier.size(32.dp)
            )
        }
        
        // 右侧按钮 - More
        FloatingActionButton(
            onClick = onMoreClick,
            modifier = Modifier.align(Alignment.CenterEnd),
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "More",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
