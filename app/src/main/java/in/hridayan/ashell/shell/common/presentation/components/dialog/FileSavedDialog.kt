package `in`.hridayan.ashell.shell.common.presentation.components.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.domain.model.SaveProgress
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.utils.getFullPathFromTreeUri
import `in`.hridayan.ashell.settings.data.SettingsKeys

@Composable
fun FileSavedDialog(
    modifier: Modifier = Modifier,
    saveProgress: SaveProgress = SaveProgress.Idle,
    onOpenFile: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val uriString = LocalSettings.current[SettingsKeys.OutputSaveDirectory]
    val saveWholeOutput = LocalSettings.current[SettingsKeys.SaveWholeOutput]

    val pathToDisplay =
        if (uriString != SettingsKeys.OutputSaveDirectory.default) {
            getFullPathFromTreeUri(
                uriString.toUri(),
                context
            ) ?: uriString
        } else uriString

    val isSaving = saveProgress is SaveProgress.Saving
    val isSuccess = saveProgress is SaveProgress.Success
    val isError = saveProgress is SaveProgress.Error

    val message = when {
        isSaving -> stringResource(R.string.saving_output)
        isError -> saveProgress.message
        else -> if (saveWholeOutput) stringResource(
            R.string.shell_output_saved_whole_message,
            pathToDisplay
        ) else stringResource(R.string.shell_output_saved_message, pathToDisplay)
    }

    val title = when {
        isSaving -> stringResource(R.string.saving)
        isError -> stringResource(R.string.failed)
        else -> stringResource(R.string.success)
    }

    Dialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = !isSaving)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                AnimatedContent(
                    targetState = title,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "title_animation"
                ) { titleText ->
                    AutoResizeableText(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = isSaving,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "content_animation"
                ) { saving ->
                    if (saving && saveProgress is SaveProgress.Saving) {
                        Column {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress = { saveProgress.progress },
                                modifier = Modifier.fillMaxWidth(),
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${saveProgress.currentLine} / ${saveProgress.totalLines}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isError) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val buttonGroupItems = listOf(
                    ButtonGroupItem(
                        buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                        text = if (isError) stringResource(R.string.close)
                        else stringResource(R.string.cancel),
                        onClick = { onDismiss() },
                        enabled = !isSaving
                    ),
                    ButtonGroupItem(
                        text = stringResource(R.string.open),
                        onClick = {
                            onOpenFile()
                            onDismiss()
                        },
                        enabled = isSuccess
                    )
                )

                OverflowButtonGroup(items = buttonGroupItems)
            }
        }
    }
}