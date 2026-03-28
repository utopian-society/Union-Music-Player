package org.bibichan.union.player.ui.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.bibichan.union.player.ui.player.models.PlayerState

@Composable
fun AudioInfoRow(
    state: PlayerState,
    modifier: Modifier = Modifier
) {
    val label = buildString {
        append(state.audioFormat)
        val bitDepth = state.bitDepth
        val sampleRate = state.sampleRateHz
        if (bitDepth != null || sampleRate != null) {
            append(" ")
            append(bitDepth ?: "--")
            append("/")
            append(sampleRate ?: "--")
            append("Hz")
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
