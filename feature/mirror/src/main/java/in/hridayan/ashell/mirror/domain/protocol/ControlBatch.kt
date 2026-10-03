package `in`.hridayan.ashell.mirror.domain.protocol

import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.InputAction
import java.io.ByteArrayOutputStream

/**
 * Packs queued control messages into one write.
 *
 * An ADB stream allows a single write in flight until the device acknowledges it, so over Wi-Fi
 * every separate write costs a network round trip. Touch moves arrive faster than that, so sending
 * them one by one builds a growing backlog of stale positions. Sending what has queued as one write,
 * minus moves that a later move of the same finger supersedes, keeps input current.
 */
object ControlBatch {

    /**
     * Drops each move a later move of the same pointer makes redundant. A move is never dropped
     * across a press or lift of its pointer, and no other message is dropped or reordered.
     */
    fun coalesce(messages: List<ControlMessage>): List<ControlMessage> {
        val supersededPointers = mutableSetOf<Long>()
        val kept = ArrayDeque<ControlMessage>()

        for (message in messages.asReversed()) {
            val touch = message as? ControlMessage.InjectTouch
            when {
                touch == null -> kept.addFirst(message)

                touch.action == InputAction.MOVE -> {
                    if (supersededPointers.add(touch.pointerId)) kept.addFirst(message)
                }

                else -> {
                    supersededPointers -= touch.pointerId
                    kept.addFirst(message)
                }
            }
        }
        return kept
    }

    /** The scrcpy control socket is a byte stream, so messages can simply be concatenated. */
    fun encode(messages: List<ControlMessage>): ByteArray {
        val bytes = ByteArrayOutputStream()
        messages.forEach { bytes.write(ControlMessageEncoder.encode(it)) }
        return bytes.toByteArray()
    }
}
