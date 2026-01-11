@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
 * Supports Apply-to-all for batch operations and shows all resolution options.
 */
@Composable
fun FileConflictDialog(
    conflict: FileConflict,
    showApplyToAll: Boolean = false,
    onResolution: (ConflictResolution, applyToAll: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var applyToAll by remember { mutableStateOf(false) }
    
    val title = if (conflict.isDirectory || conflict.sourceIsDirectory) {
        stringResource(R.string.folder_conflict_title)
    } else {
        stringResource(R.string.file_conflict_title)
    }

    val operationText = when (conflict.operationType) {
        OperationType.COPY -> stringResource(R.string.copying).lowercase().removeSuffix(".")
        OperationType.MOVE -> stringResource(R.string.moving).lowercase().removeSuffix(".")
        else -> ""
    }

    // Determine if both source and dest are directories (for merge option)
    val canMerge = conflict.sourceIsDirectory && conflict.isDirectory

    DialogContainer(onDismiss = onDismiss) {
        DialogTitle(
            text = title,
            modifier = Modifier
                .padding(vertical = Dimens.paddingMedium)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.conflict_message, conflict.fileName),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.what_to_do, operationText),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Apply to all checkbox (only show if there are more conflicts)
        if (showApplyToAll && conflict.remainingCount > 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = applyToAll,
                    onCheckedChange = { applyToAll = it }
                )
                Text(
                    text = stringResource(R.string.apply_to_all, conflict.remainingCount),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Vertical button layout
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Replace button (destructive action - error color)
            Button(
                onClick = withHaptic(HapticFeedbackType.Confirm) {
                    onResolution(ConflictResolution.REPLACE, applyToAll)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(
                    text = stringResource(R.string.replace),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Merge button (only for directory-to-directory conflicts)
            if (canMerge) {
                Button(
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        onResolution(ConflictResolution.MERGE, applyToAll)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.merge),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Keep Both button (works for both files and directories)
            Button(
                onClick = withHaptic(HapticFeedbackType.Confirm) {
                    onResolution(ConflictResolution.KEEP_BOTH, applyToAll)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = stringResource(R.string.keep_both),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Skip button (outlined - least prominent)
            OutlinedButton(
                onClick = withHaptic(HapticFeedbackType.Reject) {
                    onResolution(ConflictResolution.SKIP, applyToAll)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.skip),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
