@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.otg_adb_shell.presentation.components.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.viewmodel.OtgViewModel

@Composable
fun OtgDeviceWaitingDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onConfirm: () -> Unit = {},
    otgViewModel: OtgViewModel = hiltViewModel()
) {
    val otgState by otgViewModel.state.collectAsState()
    var cardHeight by remember { mutableStateOf(0.dp) }
    val screenDensity = LocalDensity.current

    val device = when (otgState) {
        is OtgState.DeviceFound -> (otgState as OtgState.DeviceFound).deviceName
        is OtgState.Connected -> (otgState as OtgState.Connected).deviceName
        else -> null
    }

    val dialogTitle =
        if (otgState is OtgState.DeviceFound || otgState is OtgState.Connected) stringResource(R.string.device_connected) else stringResource(
            R.string.waiting_for_device
        )

    val waitingStatusText = when (otgState) {
        is OtgState.Idle -> stringResource(R.string.connect_device_via_otg)
        is OtgState.Searching -> stringResource(R.string.searching_for_devices)
        is OtgState.PermissionDenied -> stringResource(R.string.permission_denied)
        is OtgState.Connecting -> stringResource(R.string.connecting)
        is OtgState.Disconnected -> stringResource(R.string.disconnected)
        is OtgState.Error -> stringResource(R.string.error) + ": ${(otgState as OtgState.Error).message}"
        else -> ""
    }

    LaunchedEffect(Unit) {
        otgViewModel.startScan()
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
                )

                AutoResizeableText(
                    text = dialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                )

                if (device != null) {
                    IconWithTextCard(
                        icon = painterResource(R.drawable.ic_otg),
                        text = device,
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                cardHeight = with(screenDensity) { coordinates.size.height.toDp() }
                            },
                        shape = RoundedCornerShape(cardHeight / 2),
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
                    Text(
                        text = waitingStatusText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )

                    LoadingIndicator(modifier = Modifier.size(72.dp))

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        shapes = ButtonDefaults.shapes(),
                        onClick = withHaptic(HapticFeedbackType.Reject) {
                            onDismiss()
                            otgViewModel.disconnect()
                        }) {
                        AutoResizeableText(text = stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}