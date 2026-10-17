package `in`.hridayan.ashell.core.common.domain.model.otg

sealed class OtgState {
    object Idle : OtgState()
    object Searching : OtgState()
    data class DeviceFound(val deviceName: String) : OtgState()

    /** @property serial the device's `ro.serialno`, or null when it could not be read. */
    data class Connected(val deviceName: String, val serial: String? = null) : OtgState()

    object PermissionDenied : OtgState()
    object Connecting : OtgState()
    object Disconnected : OtgState()
    object UsbManagerUnavailable : OtgState()
    data class Error(val message: String) : OtgState()
}
