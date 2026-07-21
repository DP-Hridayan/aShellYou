package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.core.ui.R
import `in`.hridayan.ashell.logcat.data.permission.LogcatPermissionHelper

/**
 * Informs the user that READ_LOGS cannot be granted via a system dialog
 * (it is a signatureOrSystem permission). Shows the ADB grant command
 * in a selectable monospace code block so the user can copy it.
 */
@Composable
fun LogcatPermissionDialog(
    onContinueAnyway: () -> Unit,
    onGranted: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.logcat_permission_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.logcat_permission_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                // Selectable ADB command block
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(10.dp),
                        text = LogcatPermissionHelper.GRANT_COMMAND,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onContinueAnyway) {
                    Text(stringResource(R.string.continue_anyway))
                }
                TextButton(onClick = onGranted) {
                    Text(stringResource(R.string.logcat_ive_granted_it))
                }
            }
        },
    )
}
