package `in`.hridayan.ashell.mirror.data

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannel
import `in`.hridayan.ashell.core.common.domain.model.RemoteFileStat
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException

/** Records what a session asks of the device and answers from scripted values. */
class FakeExternalDeviceChannel : ExternalDeviceChannel {

    override var isConnected: Boolean = true

    var remoteFiles = mutableMapOf<String, Long>()
    var pushFails = false
    val openedServices = mutableListOf<String>()
    val pushedPaths = mutableListOf<String>()

    /** Opens of `localabstract:` services fail this many times before succeeding. */
    var socketRejectionsBeforeAccept = 0

    override suspend fun openStream(service: String): Result<AdbDuplexStream> {
        openedServices += service
        if (service.startsWith("localabstract:") && socketRejectionsBeforeAccept > 0) {
            socketRejectionsBeforeAccept--
            return Result.failure(ConnectException("rejected"))
        }
        return Result.success(ClosedStream())
    }

    override suspend fun stat(remotePath: String): Result<RemoteFileStat> {
        val size = remoteFiles[remotePath]
        return Result.success(RemoteFileStat(sizeBytes = size ?: 0, exists = size != null))
    }

    override suspend fun push(source: InputStream, remotePath: String, mode: Int): Result<Unit> {
        if (pushFails) return Result.failure(IOException("disk full"))
        pushedPaths += remotePath
        remoteFiles[remotePath] = source.readBytes().size.toLong()
        return Result.success(Unit)
    }

    private class ClosedStream : AdbDuplexStream {
        override suspend fun read(): ByteArray = throw IOException("closed")
        override suspend fun write(data: ByteArray) = Unit
        override fun close() = Unit
    }
}
