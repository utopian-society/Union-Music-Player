@file:OptIn(
    androidx.compose.foundation.ExperimentalFoundationApi::class,
    dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi::class
)

package org.bibichan.union.player.ui.player.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.flow.collectLatest
import org.bibichan.union.player.MusicPlayer
import org.bibichan.union.player.ui.player.models.PlayerState

@Composable
fun FullScreenPlayer(
    state: PlayerState,
    onCollapse: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onSeek: (Long) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { 2 }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        TopBar(
            title = state.currentSong?.title ?: "Now Playing",
            subtitle = state.currentSong?.artist ?: "",
            onCollapse = onCollapse
        )

        Spacer(modifier = Modifier.height(8.dp))

        PageIndicator(
            currentPage = pagerState.currentPage,
            pageCount = 2,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .hazeEffect(state = hazeState, style = HazeMaterials.thin())
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(vertical = 16.dp, horizontal = 12.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> AlbumArtPage(state = state)
                    else -> LyricsPage(state = state)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = state.progress,
            onValueChange = { value ->
                val target = (value * state.durationMs).toLong()
                onSeek(target)
            },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatTime(state.currentTimeMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatRemainingTime(state.currentTimeMs, state.durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        AudioInfoChip(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        PlaybackControls(
            state = state,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            onToggleShuffle = onToggleShuffle,
            onCycleRepeat = onCycleRepeat,
            hazeState = hazeState
        )
    }
}

@Composable
private fun TopBar(
    title: String,
    subtitle: String,
    onCollapse: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onCollapse) {
            Icon(
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = "Collapse"
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More"
            )
        }
    }
}

@Composable
private fun PlaybackControls(
    state: PlayerState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    hazeState: HazeState
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .hazeEffect(state = hazeState, style = HazeMaterials.thin())
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (state.shuffleEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (state.isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp)
                )
            }
            IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.size(28.dp)
                )
            }
            IconButton(onClick = onCycleRepeat, modifier = Modifier.size(40.dp)) {
                val (icon, tint) = when (state.repeatMode) {
                    MusicPlayer.RepeatMode.ONE -> Icons.Default.RepeatOne to MaterialTheme.colorScheme.primary
                    MusicPlayer.RepeatMode.ALL -> Icons.Default.Repeat to MaterialTheme.colorScheme.primary
                    MusicPlayer.RepeatMode.OFF -> Icons.Default.Repeat to MaterialTheme.colorScheme.onSurfaceVariant
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Repeat",
                    tint = tint
                )
            }
        }
    }
}

@Composable
private fun AlbumArtPage(state: PlayerState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        AlbumArtCard(state = state, modifier = Modifier.fillMaxWidth(0.8f))
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = state.currentSong?.album ?: "",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = state.currentSong?.title ?: "",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = state.currentSong?.artist ?: "",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LyricsPage(state: PlayerState) {
    LyricsTextPage(lyricsText = state.lyricsText, currentTimeMs = state.currentTimeMs)
}

@Composable
private fun AudioInfoChip(state: PlayerState) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        AudioInfoRow(state = state)
    }
}

@Composable
private fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val color = if (index == currentPage) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}

@Composable
private fun AlbumArtCard(
    state: PlayerState,
    modifier: Modifier = Modifier
) {
    val albumArt = state.currentSong?.albumArt
    val albumArtPath = state.currentSong?.albumArtPath
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(6.dp)
    ) {
        val imageModifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
        when {
            albumArt != null -> {
                Image(
                    bitmap = albumArt.asImageBitmap(),
                    contentDescription = "Album art",
                    modifier = imageModifier
                )
            }
            albumArtPath != null -> {
                AsyncImage(
                    model = albumArtPath,
                    contentDescription = "Album art",
                    modifier = imageModifier
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Album art",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LyricsTextPage(
    lyricsText: String?,
    currentTimeMs: Long
) {
    val parsed = remember(lyricsText) { parseLyrics(lyricsText) }
    val lines = parsed.lines
    val timed = parsed.timed

    val listState = rememberLazyListState()
    var allowAutoScroll by remember { mutableStateOf(true) }
    var lastUserScrollMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collectLatest { isScrolling ->
                if (isScrolling) {
                    allowAutoScroll = false
                    lastUserScrollMs = System.currentTimeMillis()
                } else if (!allowAutoScroll) {
                    val now = System.currentTimeMillis()
                    if (now - lastUserScrollMs > ManualScrollCooldownMs) {
                        allowAutoScroll = true
                    }
                }
            }
    }

    val activeIndex = if (timed) {
        findActiveLyricIndex(lines, currentTimeMs)
    } else {
        -1
    }

    LaunchedEffect(activeIndex, allowAutoScroll) {
        if (allowAutoScroll && activeIndex >= 0) {
            listState.animateScrollToItem(activeIndex.coerceAtLeast(0))
        }
    }

    if (lines.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lyrics unavailable",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 18.dp),
        state = listState
    ) {
        itemsIndexed(lines) { index, line ->
            val isActive = index == activeIndex
            val color = if (isActive) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            }
            val weight = if (isActive) FontWeight.Bold else FontWeight.Medium
            val size = if (isActive) MaterialTheme.typography.titleMedium.fontSize else MaterialTheme.typography.bodyLarge.fontSize

            Text(
                text = line.text,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = weight, fontSize = size),
                color = color,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}

private data class ParsedLyrics(
    val lines: List<LyricLine>,
    val timed: Boolean
)

private data class LyricLine(
    val timeMs: Long?,
    val text: String
)

private fun parseLyrics(raw: String?): ParsedLyrics {
    if (raw.isNullOrBlank()) {
        return ParsedLyrics(emptyList(), timed = false)
    }

    val trimmed = raw.trim()
    val timeRegex = Regex("\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,2}))?\\]")
    val lines = mutableListOf<LyricLine>()
    var hasTimed = false

    trimmed.lineSequence().forEach { line ->
        val matches = timeRegex.findAll(line).toList()
        if (matches.isEmpty()) {
            val text = line.trim()
            if (text.isNotBlank()) {
                lines.add(LyricLine(timeMs = null, text = text))
            }
        } else {
            hasTimed = true
            val text = line.replace(timeRegex, "").trim()
            val lyricText = if (text.isNotBlank()) text else "..."
            matches.forEach { match ->
                val minutes = match.groupValues[1].toLongOrNull() ?: 0L
                val seconds = match.groupValues[2].toLongOrNull() ?: 0L
                val fraction = match.groupValues.getOrNull(3)?.padEnd(2, '0')?.toLongOrNull() ?: 0L
                val timeMs = (minutes * 60 + seconds) * 1000 + (fraction * 10)
                lines.add(LyricLine(timeMs = timeMs, text = lyricText))
            }
        }
    }

    val sortedLines = if (hasTimed) {
        lines.sortedBy { it.timeMs ?: Long.MAX_VALUE }
    } else {
        lines
    }

    return ParsedLyrics(sortedLines, timed = hasTimed)
}

private fun findActiveLyricIndex(lines: List<LyricLine>, positionMs: Long): Int {
    if (lines.isEmpty()) {
        return -1
    }
    var result = -1
    for (index in lines.indices) {
        val time = lines[index].timeMs ?: continue
        if (positionMs >= time) {
            result = index
        } else {
            break
        }
    }
    return result
}

private const val ManualScrollCooldownMs = 2000L

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
