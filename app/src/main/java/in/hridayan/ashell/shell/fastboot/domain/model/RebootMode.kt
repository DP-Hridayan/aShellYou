package `in`.hridayan.ashell.shell.fastboot.domain.model

enum class RebootMode(val displayName: String) {
    NORMAL("System"),
    BOOTLOADER("Bootloader"),
    RECOVERY("Recovery"),
    FASTBOOTD("Fastbootd")
}
