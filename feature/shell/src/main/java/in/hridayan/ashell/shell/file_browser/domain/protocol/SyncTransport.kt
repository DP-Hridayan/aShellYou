package `in`.hridayan.ashell.shell.file_browser.domain.protocol

import java.io.IOException

/**
 * A bidirectional byte stream carrying one ADB `sync:` session.
 *
 * Both operations must throw [IOException] once the peer has closed the stream, so a session ends
 * deterministically instead of waiting on silence.
 */
interface SyncTransport {

    @Throws(IOException::class)
    suspend fun read(): ByteArray

    @Throws(IOException::class)
    suspend fun write(data: ByteArray)

    fun close()
}

/**
 * A failure reported by the device, carrying its own message where the protocol supplied one.
 */
class SyncProtocolException(message: String) : IOException(message)
