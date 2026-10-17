package `in`.hridayan.ashell.portforward.presentation.components.card

import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.portforward.domain.model.TunnelState
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import `in`.hridayan.ashell.portforward.domain.model.WaitReason
import `in`.hridayan.ashell.portforward.presentation.model.TunnelCardActions
import `in`.hridayan.ashell.portforward.presentation.model.TunnelItem
import kotlinx.coroutines.delay

private const val TICK_MS = 1_000L
private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L

/** Shown on a stopped device tunnel whose device is not connected, which therefore cannot start. */
@Composable
fun DeviceOfflineLine(item: TunnelItem) {
    val device = item.spec.target as? TunnelTarget.Device ?: return
    if (item.runtime != null || item.canStart) return
    Text(
        text = stringResource(R.string.port_forward_error_device_not_connected, device.deviceName),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * The time left before a waiting tunnel gives up, as `4:32`. It ticks once a second for the eye and
 * is left out of accessibility, which announces state changes instead.
 */
@Composable
fun WaitCountdown(deadlineMillis: Long) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(deadlineMillis) {
        while (now < deadlineMillis) {
            delay(TICK_MS)
            now = System.currentTimeMillis()
        }
    }
    val totalSeconds = ((deadlineMillis - now).coerceAtLeast(0) / MILLIS_PER_SECOND)
    Text(
        modifier = Modifier.clearAndSetSemantics {},
        text = stringResource(
            R.string.port_forward_countdown,
            totalSeconds / SECONDS_PER_MINUTE,
            totalSeconds % SECONDS_PER_MINUTE
        ),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** True when the card should show one of [DeviceButtons]. */
fun TunnelItem.hasDeviceAction(state: TunnelState?): Boolean =
    otherConnectedDevice != null || needsReconnect(state)

/**
 * Fixes for a device tunnel: reconnect a Wireless Debugging device, which never reconnects by
 * itself, or move the tunnel to the device now connected in its place.
 */
@Composable
fun DeviceButtons(item: TunnelItem, state: TunnelState?, actions: TunnelCardActions) {
    if (item.needsReconnect(state)) {
        TextButton(onClick = withHaptic { actions.onReconnect() }) { Text(stringResource(R.string.reconnect)) }
    }
    item.otherConnectedDevice?.let { device ->
        FilledTonalButton(onClick = withHaptic { actions.onUseDevice(device) }) {
            Text(stringResource(R.string.port_forward_use_device, device.displayName))
        }
    }
}

private fun TunnelItem.needsReconnect(state: TunnelState?): Boolean {
    val device = spec.target as? TunnelTarget.Device ?: return false
    val waiting = (state as? TunnelState.Waiting)?.reason == WaitReason.Device
    return waiting && device.transport == ExternalDeviceTransport.WIFI_ADB && otherConnectedDevice == null
}
