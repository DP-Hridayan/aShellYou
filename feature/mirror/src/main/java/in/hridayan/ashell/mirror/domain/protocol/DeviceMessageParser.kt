package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.DeviceMessage

private const val TYPE_CLIPBOARD = 0
private const val TYPE_ACK_CLIPBOARD = 1
private const val TYPE_UHID_OUTPUT = 2
private const val CLIPBOARD_HEADER = 1 + Int.SIZE_BYTES
private const val ACK_LENGTH = 1 + Long.SIZE_BYTES
private const val UHID_HEADER = 1 + Short.SIZE_BYTES + Short.SIZE_BYTES
private const val UHID_LENGTH_OFFSET = 1 + Short.SIZE_BYTES

/** Turns the server-to-client half of the control socket into [DeviceMessage]s. */
class DeviceMessageParser {

    private val input = ByteAccumulator()

    @Throws(VideoStreamParser.ProtocolException::class)
    fun feed(chunk: ByteArray): List<DeviceMessage> {
        input.append(chunk)
        val messages = mutableListOf<DeviceMessage>()
        while (true) {
            messages += readNext() ?: break
        }
        return messages
    }

    private fun readNext(): DeviceMessage? {
        if (input.available < 1) return null
        val length = completeLengthOrNull() ?: return null
        if (input.available < length) return null

        return when (input.readUnsignedByte()) {
            TYPE_CLIPBOARD -> {
                val size = input.readInt()
                DeviceMessage.Clipboard(String(input.readBytes(size), Charsets.UTF_8))
            }

            TYPE_ACK_CLIPBOARD -> DeviceMessage.AckClipboard(input.readLong())

            else -> {
                val id = input.readUnsignedShort()
                val size = input.readUnsignedShort()
                DeviceMessage.UhidOutput(id, input.readBytes(size))
            }
        }
    }

    /** The whole message's length once its header has arrived, or null while it is still short. */
    private fun completeLengthOrNull(): Int? = when (val type = input.peekUnsignedByte(0)) {
        TYPE_CLIPBOARD -> if (input.available < CLIPBOARD_HEADER) {
            null
        } else {
            CLIPBOARD_HEADER + input.peekInt(1)
        }

        TYPE_ACK_CLIPBOARD -> ACK_LENGTH

        TYPE_UHID_OUTPUT -> if (input.available < UHID_HEADER) {
            null
        } else {
            UHID_HEADER + input.peekUnsignedShort(UHID_LENGTH_OFFSET)
        }

        else -> throw VideoStreamParser.ProtocolException("Unknown device message type $type")
    }
}
