package `in`.hridayan.ashell.shell.wifi_adb_shell.data.executor

import android.content.Context
import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.common.domain.model.wifiadb.WifiAdbDevice
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.ConnectionListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.data.repository.WifiAdbRepositoryImpl.PairingListener
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.AdbCommand
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.AdbCommandCatalog
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val DEVICE_LIST_HEADER = "List of devices attached"
private const val STATE_DEVICE = "device"
private const val STATE_UNKNOWN = "unknown"
private const val DEVICE_COLUMN_SEPARATOR = "\t"
private const val MODEL_PREFIX = "model:"
private const val TRANSPORT_ID = "transport_id:1"
private const val ADDRESS_SEPARATOR = ":"

/**
 * Answers the `adb` commands that a desktop ADB server would normally handle.
 *
 * There is no server in this app's connection, so these are emulated from the single active
 * connection the app already tracks.
 */
class AdbHostCommandExecutor(private val context: Context) {

    fun execute(
        command: AdbCommand.Host,
        repository: WifiAdbRepository
    ): Flow<OutputLine> = flow {
        when (command) {
            is AdbCommand.Host.Devices -> emitDevices(command.verbose, repository)
            is AdbCommand.Host.GetSerialNo -> emitSerialNumber(repository)
            is AdbCommand.Host.GetState -> emitState(repository)
            is AdbCommand.Host.GetDevPath -> emit(plainLine(STATE_UNKNOWN))
            is AdbCommand.Host.WaitForDevice -> emitWaitForDevice(repository)
            is AdbCommand.Host.Version -> emitVersion()
            is AdbCommand.Host.Help -> emitHelp()
            is AdbCommand.Host.ServerNoOp -> emit(infoLine(R.string.adb_server_not_applicable))
            is AdbCommand.Host.Connect -> emitConnect(command, repository)
            is AdbCommand.Host.Disconnect -> emitDisconnect(command, repository)
            is AdbCommand.Host.PairDevice -> emitPair(command, repository)
        }
    }

    private suspend fun FlowCollector<OutputLine>.emitDevices(
        verbose: Boolean,
        repository: WifiAdbRepository
    ) {
        emit(plainLine(DEVICE_LIST_HEADER))
        val device = repository.connectedDeviceOrNull() ?: return
        emit(plainLine(device.toDeviceListRow(verbose)))
    }

    private suspend fun FlowCollector<OutputLine>.emitSerialNumber(
        repository: WifiAdbRepository
    ) {
        val device = repository.connectedDeviceOrNull()
            ?: return emit(errorLine(R.string.adb_not_connected))
        emit(plainLine(device.serialOrAddress()))
    }

    private suspend fun FlowCollector<OutputLine>.emitState(
        repository: WifiAdbRepository
    ) {
        val state = if (repository.connectedDeviceOrNull() != null) STATE_DEVICE else STATE_UNKNOWN
        emit(plainLine(state))
    }

    private suspend fun FlowCollector<OutputLine>.emitWaitForDevice(
        repository: WifiAdbRepository
    ) {
        if (repository.connectedDeviceOrNull() == null) {
            emit(errorLine(R.string.adb_not_connected))
        }
    }

    private suspend fun FlowCollector<OutputLine>.emitVersion() {
        emit(plainLine(appVersionLine()))
        emit(infoLine(R.string.adb_direct_connection_note))
    }

    private suspend fun FlowCollector<OutputLine>.emitHelp() {
        emit(infoLine(R.string.adb_supported_commands))
        AdbCommandCatalog.commands.forEach { emit(plainLine(it)) }
    }

    private suspend fun FlowCollector<OutputLine>.emitConnect(
        command: AdbCommand.Host.Connect,
        repository: WifiAdbRepository
    ) {
        val address = command.ip + ADDRESS_SEPARATOR + command.port
        val connected = suspendCancellableCoroutine { continuation ->
            repository.connect(
                ip = command.ip,
                port = command.port,
                callback = object : ConnectionListener {
                    override fun onConnectionSuccess() {
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onConnectionFailed() {
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            )
        }

        if (connected) {
            emit(infoLine(R.string.adb_connected_to, address))
        } else {
            emit(errorLine(R.string.adb_failed_to_connect_to, address))
        }
    }

    private suspend fun FlowCollector<OutputLine>.emitDisconnect(
        command: AdbCommand.Host.Disconnect,
        repository: WifiAdbRepository
    ) {
        val address = command.address
            ?: repository.connectedDeviceOrNull()?.toAddress()
            ?: return emit(errorLine(R.string.adb_not_connected))

        repository.disconnect()
        emit(infoLine(R.string.adb_disconnected_from, address))
    }

    private suspend fun FlowCollector<OutputLine>.emitPair(
        command: AdbCommand.Host.PairDevice,
        repository: WifiAdbRepository
    ) {
        val address = command.ip + ADDRESS_SEPARATOR + command.port
        val paired = suspendCancellableCoroutine { continuation ->
            repository.pair(
                ip = command.ip,
                port = command.port,
                pairingCode = command.code,
                listener = object : PairingListener {
                    override fun onPairingSuccess() {
                        if (continuation.isActive) continuation.resume(true)
                    }

                    override fun onPairingFailed() {
                        if (continuation.isActive) continuation.resume(false)
                    }
                }
            )
        }

        if (paired) {
            emit(infoLine(R.string.adb_paired_with, address))
        } else {
            emit(errorLine(R.string.adb_failed_to_pair_with, address))
        }
    }

    private fun appVersionLine(): String {
        val versionName = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
        return context.getString(R.string.adb_version_line, versionName)
    }

    private fun WifiAdbDevice.toDeviceListRow(verbose: Boolean): String {
        val row = serialOrAddress() + DEVICE_COLUMN_SEPARATOR + STATE_DEVICE
        if (!verbose) return row
        return "$row $MODEL_PREFIX${deviceName.replace(' ', '_')} $TRANSPORT_ID"
    }

    private fun WifiAdbDevice.serialOrAddress(): String =
        serialNumber?.takeIf { it.isNotBlank() } ?: toAddress()

    private fun WifiAdbDevice.toAddress(): String = ip + ADDRESS_SEPARATOR + port

    private fun WifiAdbRepository.connectedDeviceOrNull(): WifiAdbDevice? =
        if (isConnected()) getCurrentDevice() else null

    private fun plainLine(text: String) = OutputLine(text, isError = false)

    private fun infoLine(resId: Int, vararg args: Any) =
        OutputLine(context.getString(resId, *args), isError = false)

    private fun errorLine(resId: Int, vararg args: Any) =
        OutputLine(context.getString(resId, *args), isError = true)
}
