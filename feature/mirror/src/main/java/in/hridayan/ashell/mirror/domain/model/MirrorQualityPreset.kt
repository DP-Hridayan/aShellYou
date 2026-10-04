package `in`.hridayan.ashell.mirror.domain.model

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport

private const val MEGABIT = 1_000_000
private const val NATIVE_SIZE = 0

/**
 * Named quality levels, each defined per transport because Wi-Fi has less headroom than a cable.
 * Sizes are long-edge limits, which is what the server's `max_size` means.
 */
enum class MirrorQualityPreset(
    val maxSize: Int,
    private val usbMegabits: Int,
    private val wifiMegabits: Int,
    val maxFps: Int
) {
    SAVER(maxSize = 1280, usbMegabits = 2, wifiMegabits = 2, maxFps = 30),
    BALANCED(maxSize = 1920, usbMegabits = 8, wifiMegabits = 4, maxFps = 60),
    SHARP(maxSize = NATIVE_SIZE, usbMegabits = 16, wifiMegabits = 8, maxFps = 60);

    fun toOptions(transport: ExternalDeviceTransport): MirrorOptions {
        val megabits = when (transport) {
            ExternalDeviceTransport.OTG -> usbMegabits
            ExternalDeviceTransport.WIFI_ADB -> wifiMegabits
        }
        return MirrorOptions(maxSize = maxSize, videoBitRate = megabits * MEGABIT, maxFps = maxFps)
    }
}
