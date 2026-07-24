package `in`.hridayan.ashell.shell.common.data.shell

import `in`.hridayan.ashell.core.domain.model.ExternalDeviceShell
import `in`.hridayan.ashell.core.domain.model.OtgConnection
import `in`.hridayan.ashell.core.domain.model.OtgState
import `in`.hridayan.ashell.shell.otg_adb_shell.domain.repository.OtgRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [ExternalDeviceShell] implementation that runs commands on a device
 * connected via OTG ADB.
 *
 * Delegates to [OtgRepository.runOtgCommand] which streams [OutputLine]s.
 * We map each line's text to a plain String for the emitter layer.
 */
@Singleton
class OtgDeviceShell @Inject constructor(
    private val otgRepository: OtgRepository,
) : ExternalDeviceShell {

    override fun execute(command: String): Flow<String> =
        otgRepository.runOtgCommand(command)
            .map { it.text }
            .flowOn(Dispatchers.IO)

    override val isConnected: Boolean
        get() = OtgConnection.currentState is OtgState.Connected
}
