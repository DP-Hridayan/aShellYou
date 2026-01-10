package `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import `in`.hridayan.ashell.R

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    existingNames: Set<String> = emptySet() // Pass current folder/file names
) {
    var folderName by remember { mutableStateOf("") }
    
    // Check if name already exists (case-insensitive)
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_folder)) },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text(stringResource(R.string.folder_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = nameExists || (folderName.isNotBlank() && folderName.contains("/")),
                supportingText = {
                    when {
                        nameExists -> Text(stringResource(R.string.fb_folder_already_exists))
                        folderName.contains("/") -> Text(stringResource(R.string.fb_invalid_folder_name))
                        else -> null
                    }
                }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(folderName.trim()) },
                enabled = isValidName
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
