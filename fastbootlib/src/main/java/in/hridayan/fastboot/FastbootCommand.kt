package `in`.hridayan.fastboot

/**
 * Represents a fastboot command to be sent to a device.
 * Commands are ASCII strings sent over USB bulk OUT endpoint.
 */
class FastbootCommand private constructor(
    val command: String,
    val data: ByteArray? = null
) {
    companion object {
        /** Query a bootloader variable. */
        fun getVar(name: String) = FastbootCommand("getvar:$name")

        /** Reboot the device normally. */
        fun reboot() = FastbootCommand("reboot")

        /** Reboot into the bootloader. */
        fun rebootBootloader() = FastbootCommand("reboot-bootloader")

        /** Reboot into recovery. */
        fun rebootRecovery() = FastbootCommand("reboot-recovery")

        /** Reboot into fastbootd (userspace fastboot). */
        fun rebootFastboot() = FastbootCommand("reboot-fastboot")

        /** Erase a partition. */
        fun erase(partition: String) = FastbootCommand("erase:$partition")

        /** Download data to the device and flash to partition. */
        fun flash(partition: String, data: ByteArray) = FastbootCommand("flash:$partition", data)

        /** Download and boot an image without flashing. */
        fun boot(data: ByteArray) = FastbootCommand("boot", data)

        /** Continue booting normally. */
        fun continueBooting() = FastbootCommand("continue")

        /** Send an OEM command. */
        fun oem(command: String) = FastbootCommand("oem $command")

        /** Create a raw command from a string. */
        fun raw(command: String) = FastbootCommand(command)
    }
}
