@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
fun RenameDialog(
    currentName: String,
    isDirectory: Boolean,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    DialogContainer(
        onDismiss = onDismiss
    ) {
        DialogTitle(
            text = stringResource(R.string.rename) + " " +
                    if (isDirectory) stringResource(R.string.folder)
                    else stringResource(R.string.file),
            modifier = Modifier
                .padding(bottom = Dimens.paddingMedium)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(30.dp))

        OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            label = { Text(stringResource(R.string.new_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier
            .fillMaxWidth()
            .height(24.dp))

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
                onClick = withHaptic(HapticFeedbackType.Confirm) {
                    onRename(newName)
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