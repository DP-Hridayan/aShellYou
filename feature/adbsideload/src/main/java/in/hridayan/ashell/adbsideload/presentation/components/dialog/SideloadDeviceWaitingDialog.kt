@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.adbsideload.presentation.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState
import `in`.hridayan.ashell.adbsideload.presentation.components.text.sideloadConnectionText
import `in`.hridayan.ashell.adbsideload.presentation.viewmodel.SideloadViewModel
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun SideloadDeviceWaitingDialog(
    onDismiss: () -> Unit,
    onDeviceConnected: () -> Unit,
    viewModel: SideloadViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            is SideloadState.Connected -> onDeviceConnected()
            is SideloadState.Idle,
            is SideloadState.Searching,
            is SideloadState.DeviceFound,
            is SideloadState.WrongMode -> while (true) {
                delay(RESCAN_INTERVAL)
                viewModel.startScan()
            }

            else -> Unit
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            DialogContent(
                state = state,
                onConfirm = onDeviceConnected,
                onRetry = viewModel::retryPermission,
                onDismiss = {
                    viewModel.disconnect()
                    onDismiss()
                }
            )
        }
    }
}

private val RESCAN_INTERVAL = 2.seconds

private val SideloadState.awaitsPermission: Boolean
    get() = this is SideloadState.DeviceFound || this is SideloadState.PermissionDenied

@Composable
private fun DialogContent(
    state: SideloadState,
    onConfirm: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    val connectedName = (state as? SideloadState.Connected)?.deviceName

    val title = if (connectedName != null) {
        stringResource(R.string.device_connected)
    } else {
        stringResource(R.string.waiting_for_device)
    }

    Column(
        modifier = Modifier
            .widthIn(min = 280.dp)
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        AutoResizeableText(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )

        if (connectedName != null) {
            DeviceFoundContent(
                deviceName = connectedName,
                onConfirm = onConfirm
            )
        } else {
            WaitingContent(
                statusText = sideloadConnectionText(state),
                isError = state is SideloadState.Error || state is SideloadState.PermissionDenied,
                canRetry = state.awaitsPermission,
                onRetry = onRetry,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun DeviceFoundContent(
    deviceName: String,
    onConfirm: () -> Unit,
) {
    IconWithTextCard(
        icon = painterResource(R.drawable.ic_otg),
        text = deviceName,
        shape = CustomCardShape(50),
    )
    Button(
        onClick = withHaptic(HapticFeedbackType.Confirm) { onConfirm() },
        shapes = ButtonDefaults.shapes(),
    ) {
        AutoResizeableText(text = stringResource(R.string.start))
    }
}

@Composable
private fun WaitingContent(
    statusText: String,
    isError: Boolean,
    canRetry: Boolean,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Text(
        text = statusText,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
    )
    if (!isError) {
        LoadingIndicator(modifier = Modifier.size(72.dp))
    }
    if (canRetry) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            shapes = ButtonDefaults.shapes(),
            onClick = withHaptic(HapticFeedbackType.Confirm) { onRetry() }
        ) {
            AutoResizeableText(text = stringResource(R.string.allow_usb_access))
        }
    }
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        shapes = ButtonDefaults.shapes(),
        onClick = withHaptic(HapticFeedbackType.Reject) { onDismiss() }
    ) {
        AutoResizeableText(text = stringResource(R.string.cancel))
    }
}
