@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.fastboot.presentation.components.dialog

import `in`.hridayan.ashell.core.resources.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.domain.model.FastbootState
import `in`.hridayan.ashell.shell.fastboot.presentation.viewmodel.FastbootViewModel
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FastbootDeviceWaitingDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    isAdbDeviceConnected: Boolean = false,
    adbDeviceName: String? = null,
    onBootIntoFastboot: () -> Unit = {},
    fastbootViewModel: FastbootViewModel = hiltViewModel()
) {
    val fastbootState by fastbootViewModel.state.collectAsState()

    val device = when (fastbootState) {
        is FastbootState.DeviceFound -> (fastbootState as FastbootState.DeviceFound).deviceName
        is FastbootState.Connected -> (fastbootState as FastbootState.Connected).deviceName
        else -> null
    }

    val dialogTitle =
        if (fastbootState is FastbootState.DeviceFound || fastbootState is FastbootState.Connected)
            stringResource(R.string.device_connected)
        else
            stringResource(R.string.waiting_for_device)

    val waitingStatusText = when (fastbootState) {
        is FastbootState.Idle -> stringResource(R.string.boot_device_into_fastboot)
        is FastbootState.Searching -> stringResource(R.string.searching_for_devices)
        is FastbootState.PermissionDenied -> stringResource(R.string.permission_denied)
        is FastbootState.Connecting -> stringResource(R.string.connecting)
        is FastbootState.Disconnected -> stringResource(R.string.disconnected)
        is FastbootState.Error -> stringResource(R.string.error) + ": ${(fastbootState as FastbootState.Error).message}"
        else -> ""
    }

    // Poll for devices while dialog is open and no device is connected.
    // Android doesn't reliably fire USB_DEVICE_ATTACHED when a device reboots
    // between modes (e.g. normal → bootloader, bootloader → fastbootd)
    // without a physical cable disconnect. Also handles re-plug scenarios
    // where permission is granted but state doesn't propagate.
    LaunchedEffect(fastbootState) {
        if (fastbootState !is FastbootState.Connected &&
            fastbootState !is FastbootState.Connecting &&
            fastbootState !is FastbootState.DeviceFound
        ) {
            while (true) {
                fastbootViewModel.startScan()
                delay(2000.milliseconds)
            }
        }
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
                    painter = painterResource(R.drawable.ic_mobile_loupe),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )

                AutoResizeableText(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                )

                if (device != null) {
                    // Fastboot device found — show connect button
                    IconWithTextCard(
                        icon = painterResource(R.drawable.ic_otg),
                        text = device,
                        shape = CustomCardShape(50),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    Button(
                        onClick = withHaptic(HapticFeedbackType.Confirm) {
                            onConfirm()
                        },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.heightIn(ButtonDefaults.MediumContainerHeight)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_play),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.widthIn(ButtonDefaults.IconSpacing))
                        AutoResizeableText(text = stringResource(R.string.start))
                    }
                } else {
                    // No fastboot device yet — show waiting state
                    Text(
                        text = waitingStatusText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )

                    LoadingIndicator(modifier = Modifier.size(72.dp))

                    // If an ADB device is connected via OTG, offer to reboot it into fastboot
                    if (isAdbDeviceConnected) {
                        FilledTonalButton(
                            modifier = Modifier.fillMaxWidth(),
                            shapes = ButtonDefaults.shapes(),
                            onClick = withHaptic(HapticFeedbackType.Confirm) {
                                onBootIntoFastboot()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhonelinkSetup,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.widthIn(ButtonDefaults.IconSpacing))
                            AutoResizeableText(
                                text = stringResource(R.string.boot_into_fastboot) +
                                        if (adbDeviceName != null) " ($adbDeviceName)" else ""
                            )
                        }

                        Spacer(modifier = Modifier.height(0.dp))
                    }

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        shapes = ButtonDefaults.shapes(),
                        onClick = withHaptic(HapticFeedbackType.Reject) {
                            onDismiss()
                            fastbootViewModel.disconnect()
                        }) {
                        AutoResizeableText(text = stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}
