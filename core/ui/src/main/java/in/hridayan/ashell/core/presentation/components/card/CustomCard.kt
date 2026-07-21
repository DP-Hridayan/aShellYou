package `in`.hridayan.ashell.core.presentation.components.card

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.theme.AshellYouAnimationSpecs
import `in`.hridayan.ashell.core.presentation.theme.CornerSize
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.theme.toDp

@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    shape: CustomCardShape = CustomCardDefaults.shape(),
    pressedCornerRadius: Dp = CustomCardDefaults.pressedCornerRadius,
    pressedScale: Float = CustomCardDefaults.pressedScale,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ),
    elevation: CardElevation = CardDefaults.cardElevation(),
    border: BorderStroke? = null,
    clickable: Boolean = true,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val density = LocalDensity.current
    var heightPx by remember { mutableFloatStateOf(0f) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val doCardInteractions = isPressed || isHovered

    val resolvedTopStart = shape.topStart.toDp(heightPx, density)
    val resolvedTopEnd = shape.topEnd.toDp(heightPx, density)
    val resolvedBottomStart = shape.bottomStart.toDp(heightPx, density)
    val resolvedBottomEnd = shape.bottomEnd.toDp(heightPx, density)

    val topStart by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedTopStart,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_anim"
    )

    val topEnd by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedTopEnd,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_anim"
    )

    val bottomStart by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedBottomStart,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_anim"
    )

    val bottomEnd by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedBottomEnd,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_anim"
    )

    val animatedShape = remember {
        AnimatedCornerShape(
            topStart = { topStart },
            topEnd = { topEnd },
            bottomStart = { bottomStart },
            bottomEnd = { bottomEnd }
        )
    }

    val animatedScale by animateFloatAsState(
        targetValue = if (doCardInteractions) pressedScale else 1f,
        animationSpec = AshellYouAnimationSpecs.springFloat,
        label = "scale_anim"
    )

    Card(
        modifier = modifier
            .hoverable(interactionSource)
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .onGloballyPositioned {
                heightPx = it.size.height.toFloat()
            },
        colors = colors,
        elevation = elevation,
        border = border,
        shape = animatedShape
    ) {
        Column(
            modifier = Modifier
                .clip(animatedShape)
                .combinedClickable(
                    enabled = clickable,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .indication(
                    interactionSource = interactionSource,
                    indication = ripple()
                )
        ) {
            content()
        }
    }
}

private class AnimatedCornerShape(
    private val topStart: () -> Dp,
    private val topEnd: () -> Dp,
    private val bottomStart: () -> Dp,
    private val bottomEnd: () -> Dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val ts = with(density) { topStart().coerceIn(0.dp, 100.dp).toPx() }
        val te = with(density) { topEnd().coerceIn(0.dp, 100.dp).toPx() }
        val bs = with(density) { bottomStart().coerceIn(0.dp, 100.dp).toPx() }
        val be = with(density) { bottomEnd().coerceIn(0.dp, 100.dp).toPx() }
        return Outline.Rounded(
            RoundRect(
                rect = Rect(Offset.Zero, size),
                topLeft = CornerRadius(ts, ts),
                topRight = CornerRadius(te, te),
                bottomRight = CornerRadius(be, be),
                bottomLeft = CornerRadius(bs, bs)
            )
        )
    }
}

object CustomCardDefaults {

    fun shape(
        all: Dp = 20.dp
    ) = CustomCardShape(
        topStart = CornerSize.Absolute(all),
        topEnd = CornerSize.Absolute(all),
        bottomStart = CornerSize.Absolute(all),
        bottomEnd = CornerSize.Absolute(all)
    )

    fun shape(
        top: Dp,
        bottom: Dp
    ) = CustomCardShape(top, bottom)

    fun shape(percent: Int) = CustomCardShape(percent)

    val pressedCornerRadius = 12.dp
    val pressedScale = 0.97f
}