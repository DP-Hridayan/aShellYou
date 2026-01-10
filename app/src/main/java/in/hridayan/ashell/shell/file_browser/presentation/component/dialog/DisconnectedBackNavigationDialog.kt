@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.file_browser.presentation.component.dialog

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

/**
 * Dialog shown when user presses back while wireless debugging is disconnected.
 * Offers options to go home or attempt reconnection.
 */
@Composable
fun DisconnectedBackNavigationDialog(
    modifier: Modifier = Modifier,
    onGoHome: () -> Unit,
    onReconnect: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                    text = stringResource(R.string.connection_lost),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                AutoResizeableText(
                    text = stringResource(R.string.connection_lost_back_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                ButtonGroup(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = withHaptic { onGoHome() },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f)
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.go_home)
                        )
                    }
                    
                    Button(
                        onClick = withHaptic { onReconnect() },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f)
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.reconnect)
                        )
                    }
                }
            }
        }
    }
}
