package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.model.FontImportState
import java.io.File

@Composable
fun FontImportDialog(
    state: FontImportState,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (state !is FontImportState.Saving) onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = state !is FontImportState.Saving)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                when (state) {
                    is FontImportState.InvalidFile -> InvalidFileContent(onDismiss = onDismiss)
                    is FontImportState.NamingPrompt -> NamingPromptContent(
                        prefilledName = state.prefilledName,
                        tempFilePath = state.tempFilePath,
                        onConfirm = onConfirm,
                        onDismiss = onDismiss
                    )

                    is FontImportState.Saving -> SavingContent()
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun InvalidFileContent(onDismiss: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        AutoResizeableText(
            text = stringResource(R.string.invalid_font_file),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.invalid_font_file_desc),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(24.dp))

    OverflowButtonGroup(
        items = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(),
                text = stringResource(R.string.dismiss),
                onClick = { onDismiss() }
            )
        )
    )
}

@Composable
private fun NamingPromptContent(
    prefilledName: String,
    tempFilePath: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember(prefilledName) { mutableStateOf(prefilledName) }

    val previewFontFamily = remember(tempFilePath) {
        if (tempFilePath.isNotEmpty()) {
            runCatching { FontFamily(Font(File(tempFilePath))) }.getOrNull()
        } else null
    } ?: FontFamily.Default

    AutoResizeableText(
        text = stringResource(R.string.import_font),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Font preview box — mirrors the one in FontStyleBottomSheet
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = 1.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            text = stringResource(R.string.font_display_text),
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = previewFontFamily)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = name,
        onValueChange = { name = it },
        label = { Text(stringResource(R.string.import_font_name_hint)) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )

    Spacer(modifier = Modifier.height(24.dp))

    OverflowButtonGroup(
        items = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                text = stringResource(R.string.cancel),
                onClick = { onDismiss() }
            ),
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ),
                enabled = name.isNotBlank(),
                text = stringResource(R.string.action_import),
                onClick = { onConfirm(name.trim()) }
            )
        )
    )
}

@Composable
private fun SavingContent() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Text(
            text = stringResource(R.string.import_font),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
