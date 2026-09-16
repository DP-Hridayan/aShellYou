package `in`.hridayan.ashell.adbsideload.domain.protocol

import java.io.IOException

/**
 * Random access into the package being sideloaded.
 */
interface SideloadBlockReader {
    @Throws(IOException::class)
    fun readBlock(offset: Long, length: Int): ByteArray
}
