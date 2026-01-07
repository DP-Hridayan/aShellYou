@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

@Composable
fun RestoreBackupDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    backupTime: String? = ""
) {
    val interactionSources = remember { List(2) { MutableInteractionSource() } }

    val (date, time) = (backupTime ?: "").split(" ").let {
        Pair(it.getOrNull(0) ?: "", it.getOrNull(1) ?: "")
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
                    .widthIn(min = 280.dp)
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.backup_found),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )


                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = stringResource(R.string.restore_dialog_message) + "\n" + stringResource(R.string.irreversible_action_warning),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AutoResizeableText(
                    text = stringResource(R.string.backup_time) + " : " + time,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(10.dp))

                AutoResizeableText(
                    text = stringResource(R.string.backup_date) + " : " + date,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                @Suppress("DEPRECATION")
                ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = withHaptic(HapticFeedbackType.Reject){
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
                        onClick = withHaptic(HapticFeedbackType.Confirm){
                            onConfirm()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[1]),
                        interactionSource = interactionSources[1],
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.restore),
                        )
                    }
                }
            }
        }
    }

}