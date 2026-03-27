/**
 * FloatingPlayer.kt - 浮动播放器组件
 *
 * 提供底部迷你播放器与全屏播放页的可复用内容
 */
package org.bibichan.union.player.ui.components

import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.data.MusicMetadata

@Composable
fun FloatingPlayer(
    musicPlayer: MusicPlayer,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    onBoundsChanged: ((Rect) -> Unit)? = null
) {
    val currentSong by musicPlayer.currentSongFlow.collectAsState()
    val isPlaying by musicPlayer.isPlayingFlow.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .onGloballyPositioned { coordinates ->
                onBoundsChanged?.invoke(coordinates.boundsInRoot())
            }
            .clickable { onExpand() },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        MiniPlayerContent(
            currentSong = currentSong,
            isPlaying = isPlaying,
            musicPlayer = musicPlayer
        )
    }
}

@Composable
private fun MiniPlayerContent(
    currentSong: MusicMetadata?,
    isPlaying: Boolean,
    musicPlayer: MusicPlayer
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Album Cover",
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.size(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            currentSong?.let { song ->
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } ?: run {
                Text(
                    text = "No song selected",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
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
fun FullPlayerSheetContent(
    musicPlayer: MusicPlayer,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
    expandProgress: Float = 1f,
    collapseDragOffsetY: Float = 0f
) {
    val currentSong by musicPlayer.currentSongFlow.collectAsState()
    val isPlaying by musicPlayer.isPlayingFlow.collectAsState()
    val playbackState by musicPlayer.playbackStateFlow.collectAsState()
    val lastError by musicPlayer.lastErrorFlow.collectAsState()

    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(musicPlayer) {
        while (true) {
            val currentPosition = musicPlayer.getCurrentPosition()
            val duration = musicPlayer.getDuration().coerceAtLeast(0L)
            positionMs = currentPosition
            durationMs = duration
            if (!isSeeking) {
                sliderPosition = if (duration > 0) {
                    (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
            }
            delay(500)
        }
    }

    val albumTitle = currentSong?.album?.takeIf { it.isNotBlank() } ?: "Stjornulaus nott'"
    val songTitle = currentSong?.title?.takeIf { it.isNotBlank() }
        ?: "# Ophelia (Remaster for 星の"
    val artistName = currentSong?.artist?.takeIf { it.isNotBlank() } ?: "Aimer"
    val statusText = when {
        lastError != null -> lastError
        playbackState == Player.STATE_BUFFERING -> "Buffering..."
        else -> null
    }
    val albumArtModel = remember(currentSong?.albumArtPath, currentSong?.albumArt) {
        resolveAlbumArt(currentSong)
    }
    val qualityLabel = remember(currentSong?.format, currentSong?.bitDepth, currentSong?.sampleRateHz) {
        buildQualityLabel(currentSong)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        AlbumArtBackdrop(
            albumArtModel = albumArtModel,
            expandProgress = expandProgress
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, collapseDragOffsetY.toInt()) }
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            AlbumArtCard(
                albumArtModel = albumArtModel,
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = albumTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontStyle = FontStyle.Italic,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = songTitle,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!statusText.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = TextSecondary
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Slider(
                value = sliderPosition,
                onValueChange = { value ->
                    sliderPosition = value
                    isSeeking = true
                },
                onValueChangeFinished = {
                    val target = (sliderPosition * durationMs).toLong()
                    musicPlayer.seekTo(target)
                    isSeeking = false
                },
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = AccentBlue,
                    activeTrackColor = AccentBlue,
                    inactiveTrackColor = SliderInactive
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = formatRemainingTime(positionMs, durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = qualityLabel,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.weight(1f))

            PlaybackControlsRow(
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

            Spacer(modifier = Modifier.height(18.dp))

            SecondaryControlsRow(
                onVolume = { },
                onComments = { },
                onShuffle = { },
                onPlaylist = { }
            )
        }
    }
}

@Composable
private fun AlbumArtBackdrop(
    albumArtModel: Any?,
    expandProgress: Float
) {
    val blurStrength = 6f + (18f * expandProgress.coerceIn(0f, 1f))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlue)
    ) {
        if (albumArtModel != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(albumArtModel)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .shadow(0.dp)
                    .clip(RectangleShape)
                    .graphicsLayer {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            renderEffect = android.graphics.RenderEffect
                                .createBlurEffect(blurStrength, blurStrength, android.graphics.Shader.TileMode.CLAMP)
                        }
                    },
                contentScale = ContentScale.Crop
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC0A1F3E),
                            Color(0xFF0A1F3E)
                        )
                    )
                )
        )
    }
}

@Composable
private fun AlbumArtCard(
    albumArtModel: Any?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .shadow(14.dp, shape)
            .clip(shape)
            .background(Color(0xFF102B57))
    ) {
        if (albumArtModel != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(albumArtModel)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "Album Art",
                tint = Color(0xFF9FBCE8),
                modifier = Modifier
                    .size(64.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

private fun resolveAlbumArt(song: MusicMetadata?): Any? {
    return when {
        song?.albumArtPath != null -> Uri.parse(song.albumArtPath)
        song?.albumArt != null -> song.albumArt
        else -> null
    }
}

private fun buildQualityLabel(song: MusicMetadata?): String {
    val formatLabel = song?.format?.displayName ?: "Unknown"
    val bitDepth = song?.bitDepth?.takeIf { it > 0 }?.toString() ?: "--"
    val sampleRateHz = song?.sampleRateHz?.takeIf { it > 0 }?.toString() ?: "--"
    return "$formatLabel · $bitDepth/${sampleRateHz}Hz"
}

@Composable
private fun PlaybackControlsRow(
    isPlaying: Boolean,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(26.dp),
                tint = TextPrimary
            )
        }

        FilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(AccentBlue),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = AccentBlue,
                contentColor = TextPrimary
            )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(40.dp)
            )
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(26.dp),
                tint = TextPrimary
            )
        }
    }
}

@Composable
private fun SecondaryControlsRow(
    onVolume: () -> Unit,
    onComments: () -> Unit,
    onShuffle: () -> Unit,
    onPlaylist: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onVolume) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "Volume",
                tint = TextSecondary
            )
        }
        IconButton(onClick = onComments) {
            Icon(
                imageVector = Icons.Default.ChatBubbleOutline,
                contentDescription = "Comments",
                tint = TextSecondary
            )
        }
        IconButton(onClick = onShuffle) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = "Shuffle",
                tint = TextSecondary
            )
        }
        IconButton(onClick = onPlaylist) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = "Playlist",
                tint = TextSecondary
            )
        }
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
    val buttonSize = if (large) 72.dp else 44.dp
    val iconSize = if (large) 32.dp else 20.dp
    val playButtonSize = if (large) 86.dp else 52.dp
    val playIconSize = if (large) 38.dp else 24.dp

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

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = (milliseconds / 1000).toInt().coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

private fun formatRemainingTime(positionMs: Long, durationMs: Long): String {
    if (durationMs <= 0L) {
        return "-0:00"
    }
    val remaining = (durationMs - positionMs).coerceAtLeast(0L)
    return "-${formatTime(remaining)}"
}

private val DeepBlue = Color(0xFF0A1F3E)
private val AccentBlue = Color(0xFF1E90FF)
private val SliderInactive = Color(0xFF23354F)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFFE0E0E0)
private val TextMuted = Color(0xFF888888)
