package `in`.hridayan.fastboot

import java.io.IOException
import java.io.InputStream

/**
 * An image to download to a device, streamed rather than held in memory: a partition image can be
 * larger than the heap the app is allowed.
 */
interface FastbootDataSource {

    /** Display name of the image, for progress reporting. */
    val name: String

    /** Exact number of bytes [open] will yield. The device is promised this count up front. */
    val length: Long

    @Throws(IOException::class)
    fun open(): InputStream
}
