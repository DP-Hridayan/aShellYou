package `in`.hridayan.ashell.core.presentation.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize

object AshellYouAnimationSpecs {
    const val DampingRatioMediumHighBouncy = 0.35f

    val springInt: SpringSpec<Int> = spring(
        dampingRatio = DampingRatioMediumHighBouncy,
        stiffness = Spring.StiffnessLow
    )

    val springFloat : SpringSpec<Float> = spring(
        dampingRatio = DampingRatioMediumHighBouncy,
        stiffness = Spring.StiffnessLow
    )

    val springDp : SpringSpec<Dp> = spring(
        dampingRatio = DampingRatioMediumHighBouncy,
        stiffness = Spring.StiffnessLow
    )

    val springIntSize : SpringSpec<IntSize> = spring(
        dampingRatio = DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
}