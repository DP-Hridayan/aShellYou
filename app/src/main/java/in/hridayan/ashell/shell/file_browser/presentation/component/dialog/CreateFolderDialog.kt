@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.theme.Dimens

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
    existingNames: Set<String> = emptySet()
) {
    val interactionSources = remember { List(2) { MutableInteractionSource() } }
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
            modifier = Modifier
                .padding(bottom = Dimens.paddingMedium)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
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
                    else -> null
                }
            }
        )

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
        )

        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimens.paddingLarge)
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
                enabled = isValidName,
                onClick = withHaptic(HapticFeedbackType.Confirm) {
                    onCreate(folderName.trim())
                },
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[1]),
                interactionSource = interactionSources[1],
                shapes = ButtonDefaults.shapes(),
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.rename),
                )
            }
        }
    }
}
