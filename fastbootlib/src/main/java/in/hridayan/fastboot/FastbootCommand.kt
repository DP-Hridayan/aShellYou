package `in`.hridayan.fastboot

/**
 * A fastboot command, sent to the device as an ASCII string over the bulk OUT endpoint.
 *
 * Commands that operate on an image, such as `flash` and `boot`, carry no payload here: the image
 * is streamed separately through [FastbootDeviceContext.sendCommand] so it never has to be held in
 * memory.
 */
class FastbootCommand private constructor(val command: String) {

    companion object {
        private val VALID_PARTITION = Regex("^[a-zA-Z0-9_-]{1,64}$")

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
        fun erase(partition: String): FastbootCommand {
            require(partition.matches(VALID_PARTITION)) {
                "Invalid partition name: '$partition'"
            }
            return FastbootCommand("erase:$partition")
        }

        /** Write the downloaded image to a partition. */
        fun flash(partition: String): FastbootCommand {
            require(partition.matches(VALID_PARTITION)) {
                "Invalid partition name: '$partition'"
            }
            return FastbootCommand("flash:$partition")
        }

        /** Boot the downloaded image without flashing it. */
        fun boot() = FastbootCommand("boot")

        /** Continue booting normally. */
        fun continueBooting() = FastbootCommand("continue")

        /** Send an OEM command. */
        fun oem(command: String) = FastbootCommand("oem $command")

        /** Create a raw command from a string. */
        fun raw(command: String) = FastbootCommand(command)
    }
}
