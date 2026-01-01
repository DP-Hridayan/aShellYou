@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.dialog

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalSettings
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.getFullPathFromTreeUri
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun ConfigureSaveDirectoryDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uriString = LocalSettings.current.outputSaveDirectory

    val pathToDisplay =
        if (uriString != SettingsKeys.OUTPUT_SAVE_DIRECTORY.default) {
            getFullPathFromTreeUri(
                uriString.toUri(),
                context
            ) ?: uriString
        } else uriString

    var rotationAngle by rememberSaveable { mutableFloatStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "Spin"
    )

    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )

            settingsViewModel.setString(SettingsKeys.OUTPUT_SAVE_DIRECTORY, it.toString())
        }
    }

    val onResetDirectory: () -> Unit = withHaptic {
        settingsViewModel.setString(
            SettingsKeys.OUTPUT_SAVE_DIRECTORY,
            SettingsKeys.OUTPUT_SAVE_DIRECTORY.default as String
        )
        rotationAngle -= 360f
    }

    val onPickFolder: () -> Unit = withHaptic {
        folderPickerLauncher.launch(null)
    }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FolderOpen,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = null
                )

                AutoResizeableText(
                    text = stringResource(R.string.configure_save_directory),
                    maxLines = 2,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = stringResource(R.string.des_configure_save_directory),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.largeIncreased)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.largeIncreased
                        )
                        .clickable(onClick = withHaptic { }),
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = pathToDisplay,
                        style = MaterialTheme.typography.titleSmallEmphasized,
                        maxLines = 1,
                        modifier = Modifier
                            .padding(15.dp)
                            .basicMarquee(),
                    )
                }

                OutlinedButton(
                    onClick = onResetDirectory,
                    shapes = ButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .rotate(animatedRotation)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    AutoResizeableText(
                        text = stringResource(R.string.default_folder),
                        style = MaterialTheme.typography.labelSmallEmphasized
                    )
                }

                Button(
                    onClick = onPickFolder,
                    shapes = ButtonDefaults.shapes(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    AutoResizeableText(
                        text = stringResource(R.string.folder_picker),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}