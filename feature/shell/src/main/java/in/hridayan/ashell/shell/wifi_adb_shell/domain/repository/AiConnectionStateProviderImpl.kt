package `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository

import `in`.hridayan.ashell.core.common.domain.model.OutputLine
import `in`.hridayan.ashell.core.common.domain.repository.AiConnectionStateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiConnectionStateProviderImpl @Inject constructor(
    private val wifiAdbRepository: WifiAdbRepository
) : AiConnectionStateProvider {

    override fun getWifiConnectedDeviceName(): String? {
        val device = wifiAdbRepository.getCurrentDevice() ?: return null
        return device.deviceName.takeIf { it.isNotBlank() } ?: "\${device.ip}:\${device.port}"
    }

    override fun isWifiOwnDevice(): Boolean {
        val device = wifiAdbRepository.getCurrentDevice() ?: return false
        return device.isOwnDevice
    }

    override fun getWifiPairedDevices(): List<String> {
        // RunBlocking is not ideal, but this is a suspend function in the interface?
        // Wait, the interface is NOT suspend. But getSavedDevicesFlow() returns a Flow.
        // It's better if I just use runBlocking for this quick read if needed, or I should make the interface suspend.
        return runBlocking {
            val devices = wifiAdbRepository.getSavedDevicesFlow().firstOrNull() ?: emptyList()
            devices.map { it.deviceName.takeIf { name -> name.isNotBlank() } ?: "\${it.ip}:\${it.port}" }
        }
    }

    override fun executeWifiCommand(command: String): Flow<OutputLine> {
        return wifiAdbRepository.execute(command)
    }

    override fun stopWifiCommand() {
        wifiAdbRepository.abortShell()
    }
}
