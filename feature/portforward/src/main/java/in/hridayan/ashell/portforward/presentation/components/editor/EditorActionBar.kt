package `in`.hridayan.ashell.portforward.presentation.components.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

private const val DISABLED_ALPHA = 0.38f
private val LabelHorizontalPadding = 20.dp
private val LabelGap = 10.dp
private val LabelIconSize = 24.dp

/**
 * The editor's actions, floating above the form in the app's toolbar style. The FAB is the main
 * action: start a new tunnel, or save and restart a running one. A stopped tunnel can also just be
 * saved from the toolbar.
 */
@Composable
fun EditorActionBar(isRunning: Boolean, enabled: Boolean, onSave: () -> Unit, onStart: () -> Unit) {
    val contentColor = FloatingToolbarDefaults.vibrantFloatingToolbarColors().toolbarContentColor
    val start = withHaptic { if (enabled) onStart() }
    HorizontalFloatingToolbar(
        expanded = true,
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        floatingActionButton = {
            FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = start) {
                val tint = LocalContentColor.current
                Icon(
                    painter = painterResource(if (isRunning) R.drawable.ic_refresh else R.drawable.ic_play),
                    contentDescription = stringResource(
                        if (isRunning) R.string.port_forward_save_and_restart else R.string.start
                    ),
                    tint = if (enabled) tint else tint.copy(alpha = DISABLED_ALPHA)
                )
            }
        },
        content = {
            if (isRunning) {
                ToolbarLabel(
                    R.string.port_forward_save_and_restart,
                    R.drawable.ic_refresh,
                    enabled,
                    contentColor,
                    start
                )
            } else {
                ToolbarLabel(
                    R.string.save,
                    R.drawable.ic_save,
                    enabled,
                    contentColor,
                    withHaptic { if (enabled) onSave() }
                )
            }
        }
    )
}

@Composable
private fun ToolbarLabel(label: Int, icon: Int, enabled: Boolean, contentColor: Color, onClick: () -> Unit) {
    val color = if (enabled) contentColor else contentColor.copy(alpha = DISABLED_ALPHA)
    Row(
        modifier = Modifier
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = LabelHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(LabelGap),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(LabelIconSize),
            painter = painterResource(icon),
            contentDescription = null,
            tint = color
        )
        Text(text = stringResource(label), color = color)
    }
}
