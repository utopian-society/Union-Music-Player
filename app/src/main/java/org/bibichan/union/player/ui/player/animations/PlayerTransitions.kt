package org.bibichan.union.player.ui.player.animations

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

object PlayerTransitions {
    val enter: EnterTransition = slideInVertically(
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    ) + fadeIn()

    val exit: ExitTransition = slideOutVertically(
        animationSpec = tween(
            durationMillis = 250,
            easing = FastOutLinearInEasing
        )
    ) + fadeOut()
}
