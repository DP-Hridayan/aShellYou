package `in`.hridayan.ashell.portforward.data.device

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.core.common.domain.provider.DeviceTunnelControl
import `in`.hridayan.ashell.portforward.domain.engine.TunnelController
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import javax.inject.Inject

class DeviceTunnelControlImpl @Inject constructor(
    private val controller: TunnelController
) : DeviceTunnelControl {

    override suspend fun stopTunnels(transport: ExternalDeviceTransport) {
        controller.runtimes.value.values
            .filter { (it.spec.target as? TunnelTarget.Device)?.transport == transport }
            .forEach { controller.stop(it.spec.id) }
    }
}
