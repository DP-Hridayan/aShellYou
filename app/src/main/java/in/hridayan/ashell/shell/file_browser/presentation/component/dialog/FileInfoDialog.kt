@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.shell.file_browser.domain.model.RemoteFile

@Composable
fun FileInfoDialog(
    file: RemoteFile,
    onDismiss: () -> Unit
) {
    DialogContainer(
        onDismiss = onDismiss,
    ) {
        DialogTitle(
            text = if (file.isDirectory) stringResource(R.string.folder_info)
            else stringResource(R.string.file_info),
        )

        Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            InfoRow(
                label = stringResource(R.string.name),
                value = file.name
            )
            InfoRow(
                label = stringResource((R.string.path)),
                value = file.path
            )
            InfoRow(
                label = stringResource(R.string.type),
                value = if (file.isDirectory) "Folder" else "File"
            )
            if (!file.isDirectory && file.displaySize.isNotEmpty()) {
                InfoRow(
                    label = stringResource(R.string.size),
                    value = file.displaySize
                )
            }
            if (file.lastModified.isNotEmpty()) {
                InfoRow(
                    label = stringResource(R.string.modified),
                    value = file.lastModified
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = withHaptic(HapticFeedbackType.Confirm) {
                onDismiss()
            },
            shapes = ButtonDefaults.shapes()
        ) {
            AutoResizeableText(stringResource(R.string.dismiss))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMediumEmphasized,
            color = MaterialTheme.colorScheme.primary
        )
        SelectionContainer {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
