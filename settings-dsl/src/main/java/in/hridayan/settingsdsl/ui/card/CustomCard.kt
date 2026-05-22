package `in`.hridayan.settingsdsl.ui.card

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Internal animated card composable used by all settings item views.
 *
 * Animates corner radius and scale on press/hover. Not accessible to library consumers.
 */
@Composable
internal fun CustomCard(
    modifier: Modifier = Modifier,
    shape: CustomCardShape = CustomCardShape(all = 20.dp),
    pressedCornerRadius: Dp = 12.dp,
    pressedScale: Float = 0.97f,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
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
        animationSpec = springDp, label = "corner_ts",
    )
    val topEnd by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedTopEnd,
        animationSpec = springDp, label = "corner_te",
    )
    val bottomStart by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedBottomStart,
        animationSpec = springDp, label = "corner_bs",
    )
    val bottomEnd by animateDpAsState(
        targetValue = if (doCardInteractions) pressedCornerRadius else resolvedBottomEnd,
        animationSpec = springDp, label = "corner_be",
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (doCardInteractions) pressedScale else 1f,
        animationSpec = springFloat, label = "scale",
    )

    val animatedShape = RoundedCornerShape(
        topStart = topStart.coerceIn(0.dp, 100.dp),
        topEnd = topEnd.coerceIn(0.dp, 100.dp),
        bottomStart = bottomStart.coerceIn(0.dp, 100.dp),
        bottomEnd = bottomEnd.coerceIn(0.dp, 100.dp),
    )

    Card(
        modifier = modifier
            .hoverable(interactionSource)
            .scale(animatedScale)
            .onGloballyPositioned { heightPx = it.size.height.toFloat() },
        colors = colors,
        elevation = elevation,
        border = border,
        shape = animatedShape,
    ) {
        Column(
            modifier = Modifier
                .clip(animatedShape)
                .combinedClickable(
                    enabled = clickable,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .indication(interactionSource = interactionSource, indication = ripple()),
        ) {
            content()
        }
    }
}

private const val DampingRatioMediumHighBouncy = 0.35f

private val springFloat: SpringSpec<Float> = spring(
    dampingRatio = DampingRatioMediumHighBouncy,
    stiffness = Spring.StiffnessLow
)

private val springDp: SpringSpec<Dp> = spring(
    dampingRatio = DampingRatioMediumHighBouncy,
    stiffness = Spring.StiffnessLow
)