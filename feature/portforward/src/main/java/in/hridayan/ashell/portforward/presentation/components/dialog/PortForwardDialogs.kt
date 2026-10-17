package `in`.hridayan.ashell.portforward.presentation.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

/** Picks one of [options], each shown with [label]. Choosing an option confirms it. */
@Composable
fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        DialogHeading(title)
        Column {
            options.forEachIndexed { index, option ->
                RadioOptionCard(
                    label = label(option),
                    selected = option == selected,
                    index = index,
                    count = options.size,
                    onSelect = { onSelect(option) }
                )
            }
        }
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
