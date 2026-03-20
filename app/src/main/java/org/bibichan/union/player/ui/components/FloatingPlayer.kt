/**
 * FloatingPlayer.kt - 浮动播放器组件
 *
 * 实现Apple Music风格的浮动播放器，具有展开/收起动画效果
 */

package org.bibichan.union.player.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicMetadata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingPlayer(
    musicPlayer: MusicPlayer,
    isVisible: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit
) {
    val currentSong by remember { mutableStateOf(musicPlayer.getCurrentSong()) }
    val isPlaying by remember { mutableStateOf(musicPlayer.isPlaying()) }

    // 动画状态
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "player_expand"
    )

    // 浮动播放器容器
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer {
                alpha = animatedProgress
                scaleX = 0.8f + (0.2f * animatedProgress)
                scaleY = 0.8f + (0.2f * animatedProgress)
            }
    ) {
        // 浮动播放器卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (isVisible) onCollapse() else onExpand()
                },
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            if (isVisible) {
                // 展开状态的完整播放器界面
                ExpandedPlayer(
                    musicPlayer = musicPlayer,
                    currentSong = currentSong,
                    isPlaying = isPlaying
                )
            } else {
                // 收起状态的迷你播放器
                MiniPlayer(
                    currentSong = currentSong,
                    isPlaying = isPlaying,
                    musicPlayer = musicPlayer
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    currentSong: MusicMetadata?,
    isPlaying: Boolean,
    musicPlayer: MusicPlayer
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 专辑封面占位符
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Album Cover",
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 歌曲信息
        Column(
            modifier = Modifier.weight(1f)
        ) {
            currentSong?.let { song ->
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } ?: run {
                Text(
                    text = "No song playing",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        // 播放控制按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    if (musicPlayer.isPlaying()) {
                        musicPlayer.pause()
                    } else {
                        musicPlayer.resume()
                    }
                }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }

            IconButton(
                onClick = { musicPlayer.next() }
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next"
                )
            }
        }
    }
}

@Composable
fun ExpandedPlayer(
    musicPlayer: MusicPlayer,
    currentSong: MusicMetadata?,
    isPlaying: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 专辑封面
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(1f),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 8.dp
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Album Cover",
                modifier = Modifier.padding(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 歌曲信息
        currentSong?.let { song ->
            Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = song.artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } ?: run {
            Text(
                text = "No song playing",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 播放控制按钮
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一首
            FilledIconButton(
                onClick = { musicPlayer.previous() },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(32.dp)
                )
            }

            // 播放/暂停
            FilledIconButton(
                onClick = {
                    if (musicPlayer.isPlaying()) {
                        musicPlayer.pause()
                    } else {
                        musicPlayer.resume()
                    }
                },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (musicPlayer.isPlaying()) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (musicPlayer.isPlaying()) "Pause" else "Play",
                    modifier = Modifier.size(40.dp)
                )
            }

            // 下一首
            FilledIconButton(
                onClick = { musicPlayer.next() },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
