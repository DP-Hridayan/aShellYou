package `in`.hridayan.ashell.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

fun slideFadeInFromRight(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { (it * 0.15f).toInt() },
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = 0.5f,
        animationSpec = tween(200, delayMillis = 66, easing = LinearEasing)
    )
}

fun slideFadeOutToRight(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { (it * 0.10f).toInt() },
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(50, easing = LinearEasing)
    )
}

fun slideFadeInFromLeft(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -(it * 0.15f).toInt() },
        animationSpec = tween(350, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = 0.5f,
        animationSpec = tween(50, delayMillis = 66, easing = LinearEasing)
    )
}

fun slideFadeOutToLeft(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { -(it * 0.10f).toInt() },
        animationSpec = tween(250, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(50, easing = LinearEasing)
    )
}

fun predictiveEnter(): EnterTransition {
    return slideInHorizontally(
        initialOffsetX = { -(it * 0.25f).toInt() },
        animationSpec = tween(600, easing = FastOutSlowInEasing)
    ) + fadeIn(
        initialAlpha = 0.75f,
        animationSpec = tween(100, delayMillis = 66, easing = LinearEasing)
    ) + scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(600, delayMillis = 66, easing = LinearEasing)
    )
}

fun predictiveExit(): ExitTransition {
    return slideOutHorizontally(
        targetOffsetX = { (it) },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(150, easing = LinearEasing)
    )
}