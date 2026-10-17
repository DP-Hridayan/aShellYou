package `in`.hridayan.ashell.shell.common.data.channel

import com.cgutman.adblib.AdbStream
import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgConnection
import `in`.hridayan.ashell.core.common.domain.model.otg.OtgState
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import `in`.hridayan.ashell.shell.file_browser.data.protocol.AdblibSyncTransport
import `in`.hridayan.ashell.shell.file_browser.domain.protocol.SyncTransport
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runInterruptible
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OtgDeviceChannel @Inject constructor(
    private val otgRepository: OtgRepository
) : SyncBackedDeviceChannel() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val isConnected: Boolean
        get() = otgRepository.isConnected()

    override val connectionState: StateFlow<ExternalDeviceState> = OtgConnection.state
        .map { it.toExternalDeviceState() }
        .stateIn(scope, SharingStarted.Eagerly, OtgConnection.currentState.toExternalDeviceState())

    override suspend fun openRaw(service: String, ioDispatcher: CoroutineDispatcher): AdbDuplexStream =
        AdblibDuplexStream(openAdblibStream(service), ioDispatcher)

    override suspend fun openSyncTransport(service: String): SyncTransport =
        AdblibSyncTransport(openAdblibStream(service))

    private suspend fun openAdblibStream(service: String): AdbStream {
        val connection = otgRepository.getAdbConnection()
            ?: throw IOException("No OTG ADB connection")
        return runInterruptible(Dispatchers.IO) { connection.open(service) }
    }

    private fun OtgState.toExternalDeviceState(): ExternalDeviceState = when (this) {
        is OtgState.Connected -> ExternalDeviceState.Connected(deviceId = serial, displayName = deviceName)
        else -> ExternalDeviceState.Disconnected
    }
}
