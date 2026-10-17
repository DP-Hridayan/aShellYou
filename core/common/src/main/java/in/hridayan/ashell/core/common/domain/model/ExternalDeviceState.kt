package `in`.hridayan.ashell.core.common.domain.model

/** Whether another device is reachable on one [ExternalDeviceTransport], and which one. */
sealed interface ExternalDeviceState {

    data object Disconnected : ExternalDeviceState

    /**
     * @property deviceId the device's serial number, or null when the device did not report one. Two
     * connections with the same non-null id reach the same physical device.
     */
    data class Connected(val deviceId: String?, val displayName: String) : ExternalDeviceState
}
