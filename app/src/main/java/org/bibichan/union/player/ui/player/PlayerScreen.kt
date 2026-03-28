package org.bibichan.union.player.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.chrisbanes.haze.HazeState
import org.bibichan.union.player.ui.player.components.FullScreenPlayer
import org.bibichan.union.player.ui.player.components.MiniPlayer

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()

    if (isExpanded) {
        FullScreenPlayer(
            state = state,
            onCollapse = onCollapse,
            onPlayPause = viewModel::playPause,
            onNext = viewModel::next,
            onPrevious = viewModel::previous,
            onToggleShuffle = viewModel::toggleShuffle,
            onCycleRepeat = viewModel::cycleRepeatMode,
            onSeek = viewModel::seekTo,
            hazeState = hazeState,
            modifier = modifier
        )
    } else {
        MiniPlayer(
            state = state,
            onExpand = onExpand,
            onPlayPause = viewModel::playPause,
            hazeState = hazeState,
            modifier = modifier
        )
    }
}
