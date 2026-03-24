/**
 * FloatingPlayer.kt - 浮动播放器组件
 *
 * 实现Apple Music风格的浮动播放器，具有展开/收起动画效果
 */

package org.bibichan.union.player.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    val currentSong by musicPlayer.currentSongFlow.collectAsState()
    val isPlaying by musicPlayer.isPlayingFlow.collectAsState()

    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "player_expand"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .graphicsLayer {
                alpha = 1f
                scaleX = 0.95f + (0.05f * animatedProgress)
                scaleY = 0.95f + (0.05f * animatedProgress)
            }
    ) {
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
                defaultElevation = 10.dp
            )
        ) {
            if (isVisible) {
                ExpandedPlayer(
                    musicPlayer = musicPlayer,
                    currentSong = currentSong,
                    isPlaying = isPlaying
                )
            } else {
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

        Spacer(modifier = Modifier.size(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            currentSong?.let { song ->
                androidx.compose.material3.Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                androidx.compose.material3.Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } ?: run {
                androidx.compose.material3.Text(
                    text = "No song selected",
                    style = MaterialTheme.typography.titleMedium
                )
                androidx.compose.material3.Text(
                    text = "Tap a song to play",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AppleControlRow(
            isPlaying = isPlaying,
            onPrevious = { musicPlayer.previous() },
            onPlayPause = {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.pause()
                } else {
                    musicPlayer.resume()
                }
            },
            onNext = { musicPlayer.next() }
        )
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

        Spacer(modifier = Modifier.size(32.dp))

        currentSong?.let { song ->
            androidx.compose.material3.Text(
                text = song.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.size(8.dp))

            androidx.compose.material3.Text(
                text = song.artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } ?: run {
            androidx.compose.material3.Text(
                text = "No song selected",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.size(32.dp))

        AppleControlRow(
            isPlaying = isPlaying,
            onPrevious = { musicPlayer.previous() },
            onPlayPause = {
                if (musicPlayer.isPlaying()) {
                    musicPlayer.pause()
                } else {
                    musicPlayer.resume()
                }
            },
            onNext = { musicPlayer.next() },
            large = true
        )
    }
}

@Composable
private fun AppleControlRow(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    large: Boolean = false
) {
    val buttonSize = if (large) 72.dp else 48.dp
    val iconSize = if (large) 32.dp else 22.dp
    val playButtonSize = if (large) 86.dp else 56.dp
    val playIconSize = if (large) 38.dp else 26.dp

    val controlBackground = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
        )
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(playButtonSize)
                .clip(CircleShape)
                .background(controlBackground),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(playIconSize)
            )
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(iconSize),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
