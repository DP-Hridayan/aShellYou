@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.local_adb_shell.presentation.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.components.text.CopyableCommandBlock
import `in`.hridayan.ashell.core.resources.R

/**
 * Explains how to open ADB's TCP port, either from a computer with [command] or, when
 * [onUseWirelessDebugging] is non-null, from this device through wireless debugging.
 */
@Composable
fun TcpIpUnavailableDialog(
    command: String,
    onCopyCommand: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onUseWirelessDebugging: (() -> Unit)? = null
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
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                AutoResizeableText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.tcpip_not_available_title),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(15.dp))

                DialogBodyText(text = stringResource(R.string.tcpip_not_available_instructions))

                Spacer(modifier = Modifier.height(12.dp))

                CopyableCommandBlock(command = command, onCopy = onCopyCommand)

                Spacer(modifier = Modifier.height(12.dp))

                DialogBodyText(text = stringResource(R.string.tcpip_not_available_footer))

                Spacer(modifier = Modifier.height(24.dp))

                DialogActions(onDismiss = onDismiss, onUseWirelessDebugging = onUseWirelessDebugging)
            }
        }
    }
}

@Composable
private fun DialogBodyText(text: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center
    )
}

/**
 * Stacked full width because "Use wireless debugging" does not fit beside a second button on narrow
 * screens.
 */
@Composable
private fun DialogActions(onDismiss: () -> Unit, onUseWirelessDebugging: (() -> Unit)?) {
    if (onUseWirelessDebugging == null) {
        Button(
            onClick = withHaptic { onDismiss() },
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.fillMaxWidth()
        ) {
            AutoResizeableText(text = stringResource(R.string.ok))
        }
        return
    }

    Button(
        onClick = withHaptic(HapticFeedbackType.Confirm) { onUseWirelessDebugging() },
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier.fillMaxWidth()
    ) {
        AutoResizeableText(text = stringResource(R.string.use_wireless_debugging))
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedButton(
        onClick = withHaptic { onDismiss() },
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier.fillMaxWidth()
    ) {
        AutoResizeableText(text = stringResource(R.string.cancel))
    }
}
