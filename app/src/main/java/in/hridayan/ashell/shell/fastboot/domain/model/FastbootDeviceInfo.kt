package `in`.hridayan.ashell.shell.fastboot.domain.model

data class FastbootDeviceInfo(
    val product: String? = null,
    val serialNo: String? = null,
    val variant: String? = null,
    val bootloaderVersion: String? = null,
    val basebandVersion: String? = null,
    val isUnlocked: Boolean? = null,
    val currentSlot: String? = null,
    val batteryLevel: String? = null,
    val maxDownloadSize: String? = null,
    val securityPatchLevel: String? = null
)
