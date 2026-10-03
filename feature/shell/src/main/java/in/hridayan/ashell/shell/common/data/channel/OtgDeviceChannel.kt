package `in`.hridayan.ashell.shell.common.data.channel

import com.cgutman.adblib.AdbStream
import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import `in`.hridayan.ashell.shell.file_browser.data.protocol.AdblibSyncTransport
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncTransport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtgDeviceChannel @Inject constructor(
    private val otgRepository: OtgRepository
) : SyncBackedDeviceChannel() {

    override val isConnected: Boolean
        get() = otgRepository.isConnected()

    override suspend fun openRaw(service: String): AdbDuplexStream =
        AdblibDuplexStream(openAdblibStream(service))

    override suspend fun openSyncTransport(service: String): SyncTransport =
        AdblibSyncTransport(openAdblibStream(service))

    private suspend fun openAdblibStream(service: String): AdbStream {
        val connection = otgRepository.getAdbConnection()
            ?: throw IOException("No OTG ADB connection")
        return runInterruptible(Dispatchers.IO) { connection.open(service) }
    }
}
