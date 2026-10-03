package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.DevicePosition
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

private const val TYPE_INJECT_KEYCODE = 0
private const val TYPE_INJECT_TEXT = 1
private const val TYPE_INJECT_TOUCH_EVENT = 2
private const val TYPE_INJECT_SCROLL_EVENT = 3
private const val TYPE_BACK_OR_SCREEN_ON = 4
private const val TYPE_EXPAND_NOTIFICATION_PANEL = 5
private const val TYPE_EXPAND_SETTINGS_PANEL = 6
private const val TYPE_COLLAPSE_PANELS = 7
private const val TYPE_SET_CLIPBOARD = 9
private const val TYPE_SET_DISPLAY_POWER = 10
private const val TYPE_ROTATE_DEVICE = 11
private const val TYPE_RESET_VIDEO = 17

private const val INJECT_TEXT_MAX_BYTES = 300
private const val CLIPBOARD_TEXT_MAX_BYTES = (1 shl 18) - 14
private const val SCROLL_RANGE = 16f

/**
 * Serialises [ControlMessage]s exactly as the v4.1 server's `ControlMessageReader` expects: one type
 * byte, then big-endian fields. The layout is internal to scrcpy and changes between versions.
 */
object ControlMessageEncoder {

    fun encode(message: ControlMessage): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out -> out.write(message) }
        return bytes.toByteArray()
    }

    /** Messages that are nothing but their type byte. */
    private val typeOnlyCodes: Map<ControlMessage, Int> = mapOf(
        ControlMessage.ExpandNotificationPanel to TYPE_EXPAND_NOTIFICATION_PANEL,
        ControlMessage.ExpandSettingsPanel to TYPE_EXPAND_SETTINGS_PANEL,
        ControlMessage.CollapsePanels to TYPE_COLLAPSE_PANELS,
        ControlMessage.RotateDevice to TYPE_ROTATE_DEVICE,
        ControlMessage.ResetVideo to TYPE_RESET_VIDEO
    )

    private fun DataOutputStream.write(message: ControlMessage) {
        when (message) {
            is ControlMessage.InjectKeycode -> writeKeycode(message)
            is ControlMessage.InjectText -> writeText(message)
            is ControlMessage.InjectTouch -> writeTouch(message)
            is ControlMessage.InjectScroll -> writeScroll(message)
            is ControlMessage.SetClipboard -> writeClipboard(message)
            is ControlMessage.BackOrScreenOn -> {
                writeByte(TYPE_BACK_OR_SCREEN_ON)
                writeByte(message.action)
            }

            is ControlMessage.SetDisplayPower -> {
                writeByte(TYPE_SET_DISPLAY_POWER)
                writeBoolean(message.on)
            }

            else -> writeByte(typeOnlyCodes.getValue(message))
        }
    }

    private fun DataOutputStream.writeKeycode(message: ControlMessage.InjectKeycode) {
        writeByte(TYPE_INJECT_KEYCODE)
        writeByte(message.action)
        writeInt(message.keyCode)
        writeInt(message.repeat)
        writeInt(message.metaState)
    }

    private fun DataOutputStream.writeText(message: ControlMessage.InjectText) {
        writeByte(TYPE_INJECT_TEXT)
        writeString(message.text, INJECT_TEXT_MAX_BYTES)
    }

    private fun DataOutputStream.writeTouch(message: ControlMessage.InjectTouch) {
        writeByte(TYPE_INJECT_TOUCH_EVENT)
        writeByte(message.action)
        writeLong(message.pointerId)
        writePosition(message.position)
        writeShort(WireEncoding.toUnsignedFixedPoint(message.pressure))
        writeInt(message.actionButton)
        writeInt(message.buttons)
    }

    private fun DataOutputStream.writeScroll(message: ControlMessage.InjectScroll) {
        writeByte(TYPE_INJECT_SCROLL_EVENT)
        writePosition(message.position)
        writeShort(WireEncoding.toSignedFixedPoint(message.horizontal / SCROLL_RANGE))
        writeShort(WireEncoding.toSignedFixedPoint(message.vertical / SCROLL_RANGE))
        writeInt(message.buttons)
    }

    private fun DataOutputStream.writeClipboard(message: ControlMessage.SetClipboard) {
        writeByte(TYPE_SET_CLIPBOARD)
        writeLong(message.sequence)
        writeBoolean(message.paste)
        writeString(message.text, CLIPBOARD_TEXT_MAX_BYTES)
    }

    private fun DataOutputStream.writePosition(position: DevicePosition) {
        writeInt(position.x)
        writeInt(position.y)
        writeShort(position.screenSize.width)
        writeShort(position.screenSize.height)
    }

    private fun DataOutputStream.writeString(text: String, maxBytes: Int) {
        val utf8 = text.toByteArray(Charsets.UTF_8)
        val length = WireEncoding.utf8TruncationIndex(utf8, maxBytes)
        writeInt(length)
        write(utf8, 0, length)
    }
}
