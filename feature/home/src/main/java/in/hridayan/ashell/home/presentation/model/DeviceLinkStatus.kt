package `in`.hridayan.ashell.home.presentation.model

import androidx.compose.runtime.Immutable

/**
 * Transport-agnostic connection status shown on a home screen card, so the home feature does not
 * depend on the OTG, Fastboot, Sideload or Wireless Debugging state types.
 */
@Immutable
sealed interface DeviceLinkStatus {
    data object Idle : DeviceLinkStatus
    data object Connecting : DeviceLinkStatus
    data class Connected(val deviceName: String) : DeviceLinkStatus
}

/**
 * @property isOwnDevice true when the Wireless Debugging connection targets the device running the
 * app; only meaningful while [link] is [DeviceLinkStatus.Connected].
 */
@Immutable
data class WifiAdbCardStatus(
    val link: DeviceLinkStatus = DeviceLinkStatus.Idle,
    val isOwnDevice: Boolean = false
)
