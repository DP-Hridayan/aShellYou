@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog

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
import androidx.compose.material3.Text
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
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbDevice

@Composable
fun ConnectionSuccessDialog(
    modifier: Modifier = Modifier,
    device: WifiAdbDevice?,
    onGoToTerminal: () -> Unit,
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
                    text = stringResource(R.string.success),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.successful_connection_message),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                device?.let {
                    Spacer(modifier = Modifier.height(16.dp))

                    AutoResizeableText(
                        text = it.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    AutoResizeableText(
                        text = "${it.ip}:${it.port}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = withHaptic(HapticFeedbackType.Confirm) {
                        onGoToTerminal()
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AutoResizeableText(
                        text = stringResource(R.string.go_to_terminal),
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
            }
        }
    }
}
