package `in`.hridayan.ashell.adbsideload.domain.protocol

import `in`.hridayan.ashell.adbsideload.domain.model.SideloadState

/**
 * Decides what a reachable device means for sideloading.
 *
 * Sideload mode and ordinary USB debugging expose the same USB interface, so the ADB connect
 * banner is the only signal that separates them: adbd on a booted device reports "device", while
 * recovery reports "recovery" or "sideload". An empty or unrecognised banner is treated as usable
 * so an unusual recovery is not locked out.
 */
object SideloadReadiness {

    fun stateFor(deviceName: String, banner: String): SideloadState = if (isBootedDevice(banner)) {
        SideloadState.WrongMode(deviceName)
    } else {
        SideloadState.Connected(deviceName)
    }

    /**
     * Reports whether a scan would only disturb work already under way. A scan during a connection
     * attempt cancels it, and a scan while genuinely connected would tear down a live transfer.
     */
    fun isScanRedundant(
        state: SideloadState,
        isConnecting: Boolean,
        hasLiveConnection: Boolean,
    ): Boolean = when {
        isConnecting -> true
        state is SideloadState.Connecting -> true
        state is SideloadState.Connected -> hasLiveConnection
        else -> false
    }

    private fun isBootedDevice(banner: String): Boolean =
        banner.substringBefore(BANNER_SEPARATOR).trim().equals(BOOTED_DEVICE_MODE, ignoreCase = true)

    private const val BANNER_SEPARATOR = "::"
    private const val BOOTED_DEVICE_MODE = "device"
}
