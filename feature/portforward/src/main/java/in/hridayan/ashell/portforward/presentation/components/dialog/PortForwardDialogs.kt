package `in`.hridayan.ashell.portforward.presentation.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.radio.RadioOptionCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R

/** Asks the user to confirm [confirmLabel], explained by [message]. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        DialogHeading(title)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DialogButtons(confirmLabel = confirmLabel, onConfirm = onConfirm, onDismiss = onDismiss)
    }
}

/**
 * Picks one of [options], each shown with [label]. The choice stays local to the dialog until
 * Apply passes it to [onApply]; Cancel or dismissing keeps [selected].
 */
@Composable
fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onApply: (T) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelected by remember { mutableStateOf(selected) }

    LaunchedEffect(selected) {
        tempSelected = selected
    }

    DialogContainer(onDismiss = onDismiss) {
        DialogHeading(title)
        Column {
            options.forEachIndexed { index, option ->
                RadioOptionCard(
                    label = label(option),
                    selected = option == tempSelected,
                    index = index,
                    count = options.size,
                    onSelect = { tempSelected = option }
                )
            }
        }
        DialogButtons(
            confirmLabel = stringResource(R.string.apply),
            onConfirm = { onApply(tempSelected) },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun DialogHeading(title: String) {
    AutoResizeableText(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    )
}

@Composable
private fun DialogButtons(confirmLabel: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    OverflowButtonGroup(
        modifier = Modifier.padding(top = 24.dp),
        items = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                text = stringResource(R.string.cancel),
                onClick = onDismiss
            ),
            ButtonGroupItem(text = confirmLabel, onClick = onConfirm)
        )
    )
}
