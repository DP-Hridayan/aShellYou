package `in`.hridayan.ashell.shell.common.data.shell

import `in`.hridayan.ashell.core.domain.model.ExternalDeviceShell
import `in`.hridayan.ashell.core.domain.model.WifiAdbConnection
import `in`.hridayan.ashell.core.domain.model.WifiAdbState
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.repository.WifiAdbRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ExternalDeviceShell] implementation that runs commands on a non-own device
 * connected via Wi-Fi ADB.
 *
 * [isConnected] is true only when the active Wi-Fi ADB connection belongs to
 * an *other* device (i.e. [WifiAdbDevice.isOwnDevice] == false), so the
 * Other Device tab in the logcat screen shows the right state.
 */
@Singleton
class WifiAdbOtherDeviceShell @Inject constructor(
    private val wifiAdbRepository: WifiAdbRepository,
) : ExternalDeviceShell {

    override fun execute(command: String): Flow<String> =
        wifiAdbRepository.execute(command)
            .map { it.text }
            .flowOn(Dispatchers.IO)

    override val isConnected: Boolean
        get() = WifiAdbConnection.currentState is WifiAdbState.Connected &&
                WifiAdbConnection.currentDevice.value?.isOwnDevice == false
}
