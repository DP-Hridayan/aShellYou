package `in`.hridayan.fastboot

/**
 * Listener for fastboot device lifecycle events.
 */
abstract class FastbootDeviceManagerListener {
    /** Called when a fastboot device is physically attached via USB. */
    open fun onFastbootDeviceAttached(deviceId: DeviceId) {}

    /** Called when a fastboot device is physically detached. */
    open fun onFastbootDeviceDetached(deviceId: DeviceId) {}

    /**
     * Called when a connection to a fastboot device is established.
     * The [deviceContext] can be used to send commands to the device.
     */
    open fun onFastbootDeviceConnected(deviceId: DeviceId, deviceContext: FastbootDeviceContext) {}

    /** Called when a connection to a fastboot device is lost. */
    open fun onFastbootDeviceDisconnected(deviceId: DeviceId) {}
}
