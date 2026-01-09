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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.DialogTitle
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.shell.file_browser.domain.model.ConflictResolution
import `in`.hridayan.ashell.shell.file_browser.domain.model.FileConflict
import `in`.hridayan.ashell.shell.file_browser.domain.model.OperationType

/**
 * Dialog for handling file/folder conflicts during copy/move operations.
 * Uses DialogContainer with ButtonGroup for consistent UI pattern.
 */
@Composable
fun FileConflictDialog(
    conflict: FileConflict,
    onResolution: (ConflictResolution) -> Unit,
    onDismiss: () -> Unit
) {
    val interactionSources = remember { List(3) { MutableInteractionSource() } }

    val title = if (conflict.isDirectory) {
        stringResource(R.string.fb_folder_conflict_title)
    } else {
        stringResource(R.string.fb_file_conflict_title)
    }

    val operationText = when (conflict.operationType) {
        OperationType.COPY -> stringResource(R.string.fb_copying)
        OperationType.MOVE -> stringResource(R.string.fb_moving)
        else -> ""
    }

    DialogContainer(onDismiss = onDismiss) {
        DialogTitle(
            text = title,
            modifier = Modifier
                .padding(vertical = Dimens.paddingMedium)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.fb_conflict_message, conflict.fileName),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.fb_what_to_do, operationText),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        @Suppress("DEPRECATION")
        ButtonGroup(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Skip button
            OutlinedButton(
                onClick = withHaptic(HapticFeedbackType.Reject) {
                    onResolution(ConflictResolution.SKIP)
                },
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[0]),
                shapes = ButtonDefaults.shapes(),
                interactionSource = interactionSources[0]
            ) {
                Text(
                    text = stringResource(R.string.fb_skip),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Middle button: Merge (for directories) or Keep Both (for files)
            if (conflict.isDirectory) {
                Button(
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        onResolution(ConflictResolution.MERGE)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSources[1]),
                    shapes = ButtonDefaults.shapes(),
                    interactionSource = interactionSources[1]
                ) {
                    Text(
                        text = stringResource(R.string.fb_merge),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            } else {
                Button(
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        onResolution(ConflictResolution.KEEP_BOTH)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .animateWidth(interactionSources[1]),
                    shapes = ButtonDefaults.shapes(),
                    interactionSource = interactionSources[1]
                ) {
                    Text(
                        text = stringResource(R.string.fb_keep_both),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Replace button
            Button(
                onClick = withHaptic(HapticFeedbackType.Confirm) {
                    onResolution(ConflictResolution.REPLACE)
                },
                modifier = Modifier
                    .weight(1f)
                    .animateWidth(interactionSources[2]),
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                interactionSource = interactionSources[2]
            ) {
                Text(
                    text = stringResource(R.string.fb_replace),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
