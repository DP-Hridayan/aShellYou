package `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.ui.R
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    existingNames: Set<String> = emptySet()
) {

    var folderName by remember { mutableStateOf("") }
    val nameExists by remember(folderName, existingNames) {
        derivedStateOf {
            folderName.isNotBlank() && existingNames.any {
                it.equals(folderName.trim(), ignoreCase = true)
            }
        }
    }

    val isValidName by remember(folderName, nameExists) {
        derivedStateOf {
            folderName.isNotBlank() && !nameExists && !folderName.contains("/")
        }
    }

    DialogContainer(
        onDismiss = onDismiss
    ) {
        DialogTitle(
            text = stringResource(R.string.create_folder),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        )

        OutlinedTextField(
            value = folderName,
            onValueChange = { folderName = it },
            label = { Text(stringResource(R.string.folder_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = nameExists || (folderName.isNotBlank() && folderName.contains("/")),
            supportingText = {
                when {
                    nameExists -> Text(stringResource(R.string.folder_already_exists))
                    folderName.contains("/") -> Text(stringResource(R.string.invalid_folder_name))
                }
            }
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
        )

        val buttonGroupItems = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                text = stringResource(R.string.cancel),
                onClick = { onDismiss() }
            ),
            ButtonGroupItem(
                text = stringResource(R.string.rename),
                onClick = { onCreate(folderName.trim()) },
                enabled = isValidName
            )
        )

        OverflowButtonGroup(items = buttonGroupItems)
    }
}
