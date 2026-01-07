@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.BulletPointsTextLayout
import `in`.hridayan.ashell.core.utils.splitStringToLines

@Composable
fun ReconnectFailedDialog(
    modifier: Modifier = Modifier,
    showDevOptionsButton: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = stringResource(R.string.reconnect_failed),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AutoResizeableText(
                    text = stringResource(R.string.possible_reasons) + ":",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(15.dp))

                BulletPointsTextLayout(
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    textLines = splitStringToLines(stringResource(R.string.reconnect_failed_message)),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (showDevOptionsButton) {
                    Button(
                        onClick = withHaptic(HapticFeedbackType.Confirm) {
                            onConfirm()
                            onDismiss()
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.developer_options),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = withHaptic { onDismiss() },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.dismiss),
                        )
                    }
                } else {
                    Button(
                        onClick = withHaptic { onDismiss() },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.dismiss),
                        )
                    }
                }
            }
        }
    }
}