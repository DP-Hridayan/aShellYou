@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.logcat.presentation.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.presentation.model.LogSelectionActions

/**
 * Replaces [LogcatTopBar] while log rows are being selected, styled like the file browser's
 * selection bar. Details appear only for a single row; copy and export need at least one row and
 * no export already running.
 */
@Composable
fun LogcatSelectionTopBar(
    selectedCount: Int,
    allSelected: Boolean,
    isExporting: Boolean,
    actions: LogSelectionActions,
    modifier: Modifier = Modifier,
) {
    val canAct = selectedCount > 0 && !isExporting

    TopAppBar(
        modifier = modifier,
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        ),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        title = { Text(pluralStringResource(R.plurals.selected_count, selectedCount, selectedCount)) },
        navigationIcon = {
            IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) { actions.onClose() }) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = stringResource(R.string.close))
            }
        },
        actions = {
            if (selectedCount == 1) {
                SelectionAction(R.drawable.ic_info, R.string.info, enabled = true, onClick = actions.onDetails)
            }
            SelectionAction(
                icon = if (allSelected) R.drawable.ic_deselect_all else R.drawable.ic_select_all,
                label = if (allSelected) R.string.deselect_all else R.string.select_all,
                enabled = true,
                onClick = actions.onToggleSelectAll,
            )
            SelectionAction(R.drawable.ic_copy, R.string.copy, canAct, actions.onCopy)
            SelectionAction(R.drawable.ic_share, R.string.share, canAct, actions.onShare)
            SelectionAction(R.drawable.ic_save, R.string.save, canAct, actions.onSave)
        },
    )
}

@Composable
private fun SelectionAction(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = withHaptic(HapticFeedbackType.VirtualKey) { onClick() }, enabled = enabled) {
        Icon(painter = painterResource(icon), contentDescription = stringResource(label))
    }
}
