@file:OptIn(dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi::class)

package org.bibichan.union.player.ui.player.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import org.bibichan.union.player.ui.player.models.PlayerState

@Composable
fun MiniPlayer(
    state: PlayerState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    // 使用專輯主色調創建動態背景
    val dynamicBackground = remember(state.dominantColor) {
        state.dominantColor.copy(alpha = 0.15f)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .hazeEffect(state = hazeState, style = HazeMaterials.thin())
            .clickable { onExpand() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = dynamicBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 專輯封面 - 使用動態邊框顏色
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = state.dominantColor.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = state.dominantColor.copy(alpha = 0.5f)
                )
            ) {
                val albumArt = state.currentSong?.albumArt
                val albumArtPath = state.currentSong?.albumArtPath
                when {
                    albumArt != null -> {
                        AsyncImage(
                            model = albumArt,
                            contentDescription = "Album cover",
                            modifier = Modifier.clip(CircleShape)
                        )
                    }
                    albumArtPath != null -> {
                        AsyncImage(
                            model = albumArtPath,
                            contentDescription = "Album cover",
                            modifier = Modifier.clip(CircleShape)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Album cover",
                            tint = state.dominantColor,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = state.currentSong?.title ?: "No song selected",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = state.currentSong?.artist ?: "Tap a song to play",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                MiniProgressBar(
                    progress = state.progress,
                    progressColor = state.dominantColor
                )
            }

            // 播放按鈕 - 使用動態顏色
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    tint = state.dominantColor
                )
            }

            Icon(
                imageVector = Icons.Default.ExpandLess,
                contentDescription = "Expand",
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MiniProgressBar(
    progress: Float,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(3.dp)
                .background(progressColor)
        )
    }
}
