package `in`.hridayan.ashell.core.presentation.components.card

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.theme.AshellYouAnimationSpecs
import `in`.hridayan.ashell.core.presentation.theme.CornerSize
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.theme.toDp

@Composable
fun CustomCard(
    modifier: Modifier = Modifier,
    shape: CustomCardShape = CustomCardDefaults.shape(),
    pressedCornerRadius: Dp = CustomCardDefaults.pressedCornerRadius,
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

    val resolvedTopStart = shape.topStart.toDp(heightPx, density)
    val resolvedTopEnd = shape.topEnd.toDp(heightPx, density)
    val resolvedBottomStart = shape.bottomStart.toDp(heightPx, density)
    val resolvedBottomEnd = shape.bottomEnd.toDp(heightPx, density)

    val topStart by animateDpAsState(
        targetValue = if (isPressed) pressedCornerRadius else resolvedTopStart,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_anim"
    )

    val topEnd by animateDpAsState(
        targetValue = if (isPressed) pressedCornerRadius else resolvedTopEnd,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_anim"
    )

    val bottomStart by animateDpAsState(
        targetValue = if (isPressed) pressedCornerRadius else resolvedBottomStart,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_anim"
    )

    val bottomEnd by animateDpAsState(
        targetValue = if (isPressed) pressedCornerRadius else resolvedBottomEnd,
        animationSpec = AshellYouAnimationSpecs.springDp,
        label = "corner_anim"
    )

    val animatedShape = RoundedCornerShape(
        topStart = topStart.coerceIn(0.dp, 100.dp),
        topEnd = topEnd.coerceIn(0.dp, 100.dp),
        bottomStart = bottomStart.coerceIn(0.dp, 100.dp),
        bottomEnd = bottomEnd.coerceIn(0.dp, 100.dp)
    )

    Card(
        modifier = modifier.onGloballyPositioned {
            heightPx = it.size.height.toFloat()
        },
        colors = colors,
        elevation = elevation,
        border = border,
        shape = animatedShape
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(animatedShape)
                .combinedClickable(
                    enabled = clickable,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = withHaptic { onClick() },
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
}