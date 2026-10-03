package `in`.hridayan.ashell.core.common.domain.model

import java.io.IOException

/**
 * One open ADB stream to a service on another device, such as `shell:` or `localabstract:<name>`.
 *
 * Reads return whatever payload has arrived, not a protocol frame, so callers reassemble their own
 * messages. Both operations throw [IOException] once either side has closed the stream, which is how a
 * session learns that the peer went away.
 */
interface AdbDuplexStream {

    @Throws(IOException::class)
    suspend fun read(): ByteArray

    @Throws(IOException::class)
    suspend fun write(data: ByteArray)

    fun close()
}
