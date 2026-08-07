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
import `in`.hridayan.ashell.adbsideload.presentation.viewmodel.SideloadViewModel
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.resources.R

@Composable
fun SideloadDeviceWaitingDialog(
    onDismiss: () -> Unit,
    onDeviceConnected: () -> Unit,
    viewModel: SideloadViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is SideloadState.Connected) {
            onDeviceConnected()
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
                onDismiss = {
                    viewModel.disconnect()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun DialogContent(
    state: SideloadState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val deviceName = when (state) {
        is SideloadState.DeviceFound -> state.deviceName
        is SideloadState.Connected -> state.deviceName
        else -> null
    }

    val title = if (deviceName != null)
        stringResource(R.string.device_connected)
    else
        stringResource(R.string.waiting_for_device)

    val statusText = when (state) {
        is SideloadState.Idle -> stringResource(R.string.put_device_in_sideload_mode)
        is SideloadState.Searching -> stringResource(R.string.searching_for_devices)
        is SideloadState.PermissionDenied -> stringResource(R.string.permission_denied)
        is SideloadState.Connecting -> stringResource(R.string.connecting)
        is SideloadState.Disconnected -> stringResource(R.string.disconnected)
        is SideloadState.Error -> "${stringResource(R.string.error)}: ${state.message}"
        else -> ""
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

        if (deviceName != null) {
            DeviceFoundContent(
                deviceName = deviceName,
                onConfirm = onConfirm
            )
        } else {
            WaitingContent(
                statusText = statusText,
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
    onDismiss: () -> Unit,
) {
    Text(
        text = statusText,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    LoadingIndicator(modifier = Modifier.size(72.dp))
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        shapes = ButtonDefaults.shapes(),
        onClick = withHaptic(HapticFeedbackType.Reject) { onDismiss() }
    ) {
        AutoResizeableText(text = stringResource(R.string.cancel))
    }
}
