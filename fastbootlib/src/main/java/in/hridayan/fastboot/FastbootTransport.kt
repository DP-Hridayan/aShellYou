package `in`.hridayan.fastboot

/**
 * Byte level link to a fastboot device.
 *
 * Implementations own their own timeouts and retries, so the protocol layer above deals only in
 * whole commands and whole response packets.
 */
interface FastbootTransport {

    /** Writes every byte of the range, or throws. */
    @Throws(FastbootException::class)
    fun writeFully(data: ByteArray, offset: Int, length: Int)

    /**
     * Reads one response packet.
     *
     * @return the number of bytes placed at the start of [buffer]
     */
    @Throws(FastbootException::class)
    fun readPacket(buffer: ByteArray): Int

    fun close()
}
