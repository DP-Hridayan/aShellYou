package `in`.hridayan.ashell.ai.presentation.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R

@Composable
fun RenameAiSessionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    session: ChatSessionEntity,
) {
    var newTitle by remember { mutableStateOf(session.title) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename)) },
        text = {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = withHaptic { if (newTitle.isNotBlank()) onConfirm(newTitle) }
            ) {
                Text(stringResource(R.string.save))
            }
        },

        dismissButton = {
            TextButton(onClick = withHaptic { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}