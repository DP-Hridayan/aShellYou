@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle

@Composable
fun SelectBackupFolderDialog(
    onDismiss: () -> Unit,
    onSelectFolder: () -> Unit,
) {
    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    DialogContainer(
        onDismiss = onDismiss,
    ) {
        DialogTitle(text = stringResource(R.string.select_backup_folder_title))

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.select_backup_folder_message),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = withHaptic(HapticFeedbackType.Reject) {
                    onDismiss()
                },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[0]),
                interactionSource = interactionSources[0],
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Button(
                onClick = withHaptic(HapticFeedbackType.Confirm) {
                    onSelectFolder()
                },
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[1]),
                interactionSource = interactionSources[1],
                shapes = ButtonDefaults.shapes(),
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.select_folder),
                )
            }
        }
    }
}
