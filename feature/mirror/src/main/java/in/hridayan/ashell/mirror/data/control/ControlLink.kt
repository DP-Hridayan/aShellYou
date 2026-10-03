package `in`.hridayan.ashell.mirror.data.control

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.protocol.ControlBatch
import `in`.hridayan.ashell.mirror.domain.protocol.DeviceMessageParser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Touch and key messages are at most 32 bytes, so a full batch of them stays inside the 4 KB packet
 * limit of the oldest devices.
 */
private const val MAX_BATCH_MESSAGES = 64

/**
 * The control socket of one session: messages queued by the UI are written in order by a single
 * writer, and the server's replies are drained so the socket never backs up.
 *
 * Input is only accepted while [isAccepting]; anything sent before a session streams, or after the
 * device refused injection, is dropped rather than replayed later out of context.
 */
class ControlLink(private val ioDispatcher: CoroutineDispatcher) {

    private val outgoing = Channel<ControlMessage>(Channel.UNLIMITED)

    @Volatile
    var isAccepting = false

    fun send(message: ControlMessage) {
        if (isAccepting) outgoing.trySend(message)
    }

    /** Runs until cancelled or until the socket closes. */
    suspend fun pump(control: AdbDuplexStream) {
        while (outgoing.tryReceive().isSuccess) Unit
        isAccepting = true
        try {
            coroutineScope {
                launch(ioDispatcher) { writeAll(control) }
                launch(ioDispatcher) { drainReplies(control) }
            }
        } finally {
            isAccepting = false
        }
    }

    /** Everything queued while the previous write was in flight goes out as the next single write. */
    private suspend fun writeAll(control: AdbDuplexStream) {
        try {
            while (true) {
                val batch = mutableListOf(outgoing.receive())
                while (batch.size < MAX_BATCH_MESSAGES) {
                    batch += outgoing.tryReceive().getOrNull() ?: break
                }
                control.write(ControlBatch.encode(ControlBatch.coalesce(batch)))
            }
        } catch (_: IOException) {
        }
    }

    /** Nothing asks for clipboard or UHID output yet, but unread replies would fill the socket. */
    private suspend fun drainReplies(control: AdbDuplexStream) {
        val parser = DeviceMessageParser()
        try {
            while (true) parser.feed(control.read())
        } catch (_: IOException) {
        }
    }
}
