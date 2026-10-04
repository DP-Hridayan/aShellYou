package `in`.hridayan.ashell.shell.file_browser.testing

import android.graphics.Bitmap
import com.cgutman.adblib.AdbConnection
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.common.domain.repository.OtgRepository
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.ConnectionListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.MdnsDiscoveryCallback
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.PairingListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.ReconnectListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.DiscoveredPairingService
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import kotlinx.coroutines.flow.Flow

private const val UNUSED = "not used by file browser tests"

/** A Wi-Fi ADB repository with no device, for view model tests that never reconnect. */
class DisconnectedWifiAdbRepository : WifiAdbRepository {
    override fun getCurrentDevice(): WifiAdbDevice? = null
    override fun isConnected(): Boolean = false
    override fun pair(ip: String, port: Int, pairingCode: String, listener: PairingListener?) =
        error(UNUSED)

    override fun connect(ip: String?, port: Int, callback: ConnectionListener?) = error(UNUSED)
    override fun execute(commandText: String): Flow<OutputLine> = error(UNUSED)
    override fun abortShell() = error(UNUSED)
    override fun stopMdnsDiscovery() = error(UNUSED)
    override fun getSavedDevicesFlow(): Flow<List<WifiAdbDevice>> = error(UNUSED)
    override suspend fun saveDevice(device: WifiAdbDevice) = error(UNUSED)
    override suspend fun updateDevice(device: WifiAdbDevice) = error(UNUSED)
    override suspend fun removeDevice(device: WifiAdbDevice) = error(UNUSED)
    override fun reconnect(device: WifiAdbDevice, listener: ReconnectListener?) = error(UNUSED)
    override fun cancelReconnect() = error(UNUSED)
    override fun disconnect() = error(UNUSED)
    override fun disconnect(publishState: Boolean) = error(UNUSED)
    override fun forgetDevice(device: WifiAdbDevice) = error(UNUSED)
    override fun startHeartbeat() = error(UNUSED)
    override fun stopHeartbeat() = error(UNUSED)
    override suspend fun generatePairingQR(sessionId: String, pairingCode: String, size: Int): Bitmap =
        error(UNUSED)

    override fun pairingWithQr(
        pairingCode: String,
        autoPair: Boolean,
        callback: MdnsDiscoveryCallback?
    ) = error(UNUSED)

    override fun startCodePairingDiscovery(
        onPairingServiceFound: (DiscoveredPairingService) -> Unit,
        onPairingServiceLost: (serviceName: String) -> Unit
    ) = error(UNUSED)

    override fun stopCodePairingDiscovery() = error(UNUSED)
    override fun pairAndConnect(
        ip: String,
        pairingPort: Int,
        pairingCode: String,
        callback: MdnsDiscoveryCallback?
    ) = error(UNUSED)
}

/** An OTG repository with nothing plugged in. */
class DisconnectedOtgRepository : OtgRepository {
    override fun searchDevices() = error(UNUSED)
    override fun disconnect() = error(UNUSED)
    override fun runOtgCommand(command: String): Flow<OutputLine> = error(UNUSED)
    override fun stopCommand() = error(UNUSED)
    override fun isConnected(): Boolean = false
    override fun getAdbConnection(): AdbConnection? = null
}
