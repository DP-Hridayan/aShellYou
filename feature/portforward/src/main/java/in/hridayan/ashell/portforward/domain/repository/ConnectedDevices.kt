package `in`.hridayan.ashell.portforward.domain.repository

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceState
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import kotlinx.coroutines.flow.StateFlow

/** Which device, if any, each transport reaches right now. */
interface ConnectedDevices {
    fun state(transport: ExternalDeviceTransport): StateFlow<ExternalDeviceState>
}
