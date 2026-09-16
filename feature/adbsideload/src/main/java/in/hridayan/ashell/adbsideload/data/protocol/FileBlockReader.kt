package `in`.hridayan.ashell.adbsideload.data.protocol

import `in`.hridayan.ashell.adbsideload.domain.protocol.SideloadBlockReader
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class FileBlockReader(private val channel: FileChannel) : SideloadBlockReader {

    override fun readBlock(offset: Long, length: Int): ByteArray {
        val buffer = ByteBuffer.allocate(length)
        var position = offset
        while (buffer.hasRemaining()) {
            val read = channel.read(buffer, position)
            if (read < 0) break
            position += read
        }
        return buffer.array().copyOf(buffer.position())
    }
}
