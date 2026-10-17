package `in`.hridayan.ashell.logcat.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.presentation.model.LogExportActions

/** The ⋮ button of the logcat top bar, with selecting, saving and sharing the visible logs. */
@Composable
fun LogcatOverflowMenu(actions: LogExportActions) {
    var expanded by remember { mutableStateOf(false) }
    val runAndClose: (() -> Unit) -> () -> Unit = { action ->
        {
            expanded = false
            action()
        }
    }

    Box {
        IconButton(onClick = withHaptic { expanded = true }) {
            Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = stringResource(R.string.more))
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            OverflowItem(
                label = R.string.select_logs,
                icon = R.drawable.ic_select_all,
                enabled = actions.isEnabled,
                onClick = runAndClose(actions.onSelect),
            )
            OverflowItem(
                label = R.string.save_to_file,
                icon = R.drawable.ic_save,
                enabled = actions.isEnabled,
                onClick = runAndClose(actions.onSave),
            )
            OverflowItem(
                label = R.string.share,
                icon = R.drawable.ic_share,
                enabled = actions.isEnabled,
                onClick = runAndClose(actions.onShare),
            )
        }
    }
}

@Composable
private fun OverflowItem(
    @StringRes label: Int,
    @DrawableRes icon: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(label)) },
        leadingIcon = { Icon(painter = painterResource(icon), contentDescription = null) },
        enabled = enabled,
        onClick = withHaptic(HapticFeedbackType.VirtualKey) { onClick() },
    )
}
