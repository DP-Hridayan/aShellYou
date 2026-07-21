package `in`.hridayan.ashell.settings.presentation.components.dialog

import `in`.hridayan.ashell.core.resources.R

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType

@Composable
fun SelectBackupFolderDialog(
    onDismiss: () -> Unit,
    onSelectFolder: () -> Unit,
) {

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

        OverflowButtonGroup(
            items = listOf(
                ButtonGroupItem(
                    buttonConfig = ButtonConfigDefaults.defaultConfig(
                        type = ButtonType.OutlinedButton
                    ),
                    text = stringResource(R.string.cancel),
                    onClick = { onDismiss() }
                ),
                ButtonGroupItem(
                    text = stringResource(R.string.select_folder),
                    onClick = { onSelectFolder() }
                ),
            )
        )
    }
}
