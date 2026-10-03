package `in`.hridayan.ashell.mirror.domain.model

/** A message from the server to this app on the control socket. */
sealed interface DeviceMessage {

    data class Clipboard(val text: String) : DeviceMessage

    /** Confirms the [ControlMessage.SetClipboard] with the same [sequence] was applied. */
    data class AckClipboard(val sequence: Long) : DeviceMessage

    /** Not a data class, for the same reason as [MediaPacket]: its payload is an array. */
    @Suppress("UseDataClass")
    class UhidOutput(val id: Int, val data: ByteArray) : DeviceMessage
}
