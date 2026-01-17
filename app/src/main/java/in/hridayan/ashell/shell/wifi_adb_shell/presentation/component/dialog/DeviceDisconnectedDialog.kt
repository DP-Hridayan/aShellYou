@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.dialog

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import `in`.hridayan.ashell.core.utils.isConnectedToWifi
import `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.viewmodel.WifiAdbViewModel

@Composable
fun DeviceDisconnectedDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: WifiAdbViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val res = LocalResources.current
    val interactionSources = remember { List(2) { MutableInteractionSource() } }
    val lastConnectedDevice by viewModel.lastConnectedDevice.collectAsState()
    val deviceName = lastConnectedDevice?.deviceName ?: "unknown_device"
    var isReconnecting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { if (!isReconnecting) onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = !isReconnecting)
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
                    text = stringResource(R.string.device_disconnected),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.height(16.dp))

                AutoResizeableText(
                    text = if (lastConnectedDevice?.deviceName != null) {
                        stringResource(R.string.device_disconnected_message_with_name, deviceName)
                    } else {
                        stringResource(R.string.device_disconnected_message)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                @Suppress("DEPRECATION")
                ButtonGroup(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Dimens.paddingLarge)
                ) {
                    OutlinedButton(
                        onClick = withHaptic(HapticFeedbackType.Reject) {
                            onDismiss()
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[0]),
                        interactionSource = interactionSources[0],
                        enabled = !isReconnecting
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.dismiss),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    Button(
                        onClick = withHaptic(HapticFeedbackType.Confirm) {
                            // Check WiFi connectivity first
                            if (!context.isConnectedToWifi()) {
                                Toast.makeText(
                                    context,
                                    res.getString(R.string.no_wifi_connection),
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@withHaptic
                            }

                            lastConnectedDevice?.let { device ->
                                isReconnecting = true
                                viewModel.reconnectToDeviceWithCallback(
                                    device = device,
                                    onSuccess = {
                                        isReconnecting = false
                                        Toast.makeText(
                                            context,
                                            res.getString(R.string.reconnect_success),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        onDismiss()
                                    },
                                    onFailure = { requiresPairing ->
                                        isReconnecting = false
                                        val message = if (requiresPairing) {
                                            res.getString(R.string.reconnect_failed_requires_pairing)
                                        } else {
                                            res.getString(R.string.reconnect_failed)
                                        }
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[1]),
                        interactionSource = interactionSources[1],
                        shapes = ButtonDefaults.shapes(),
                        enabled = !isReconnecting
                    ) {
                        if (isReconnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            AutoResizeableText(
                                text = stringResource(R.string.reconnect),
                            )
                        }
                    }
                }
            }
        }
    }
}
