package `in`.hridayan.ashell.adbsideload.domain.protocol

import java.io.IOException

/**
 * A bidirectional byte stream to recovery. Both operations must throw [IOException] once the
 * peer has closed the stream so the session can finish deterministically.
 */
interface SideloadTransport {
    val maxWriteSize: Int

    @Throws(IOException::class)
    suspend fun read(): ByteArray

    @Throws(IOException::class)
    suspend fun write(data: ByteArray)

    fun close()
}
