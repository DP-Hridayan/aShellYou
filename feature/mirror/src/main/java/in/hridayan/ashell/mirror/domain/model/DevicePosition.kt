package `in`.hridayan.ashell.mirror.domain.model

/**
 * A point on the device's screen in video pixels, together with the video size it was computed
 * against. The server rejects positions whose size differs from the current capture session.
 */
data class DevicePosition(val x: Int, val y: Int, val screenSize: VideoSize)
