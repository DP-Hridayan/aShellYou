package `in`.hridayan.ashell.core.presentation.components.snackbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Renders the currently active [SnackBarEvent] with a slide-up/down animation.
 * The animation replays cleanly whenever [event] changes identity (via [key]).
 *
 * @param event The event to render, or null when nothing should be shown.
 * @param onActionClicked Called when the action button is clicked.
 * @param onDismiss Called when the snackbar auto-dismisses or is cleared.
 * @param modifier Optional modifier for the host container.
 */
@Composable
fun AnimatedSnackBar(
    modifier: Modifier = Modifier,
    event: SnackBarEvent?,
    onActionClicked: () -> Unit,
    onDismiss: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(event) {
        visible = event != null
    }

    AnimatedVisibility(
        visible = visible && event != null,
        modifier = modifier,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            ),
            initialOffsetY = { it }
        ) + fadeIn(tween(200)),
        exit = slideOutVertically(
            animationSpec = tween(200),
            targetOffsetY = { it }
        ) + fadeOut(tween(150)),
    ) {
        key(event) {
            event?.let { e ->
                SnackBarContent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
                    event = e,
                    onActionClicked = onActionClicked,
                    onDismiss = onDismiss,
                )
            }
        }
    }
}

@Composable
private fun SnackBarContent(
    modifier: Modifier = Modifier,
    event: SnackBarEvent,
    onActionClicked: () -> Unit,
    onDismiss: () -> Unit,
) {
    LaunchedEffect(event) {
        delay(event.durationMillis.milliseconds)
        onDismiss()
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = event.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                modifier = Modifier.weight(1f),
            )

            if (event is SnackBarEvent.WithAction) {
                Button(
                    onClick = withHaptic(HapticFeedbackType.Confirm) { onActionClicked() },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .heightIn(min = ButtonDefaults.ExtraSmallContainerHeight),
                ) {
                    Text(
                        text = event.actionText,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}