package `in`.hridayan.fastboot

/**
 * Represents a unique identifier for a USB-connected fastboot device.
 * Wraps the USB device name (e.g., "/dev/bus/usb/001/002").
 */
data class DeviceId(val id: String) {
    override fun toString(): String = id
}
