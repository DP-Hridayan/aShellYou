package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import javax.inject.Inject

/** Connects each kind of target with the connector that knows how to reach it. */
class RoutingTargetConnector @Inject constructor(
    private val network: NetworkTargetConnector,
    private val device: DeviceTargetConnector
) : TargetConnector {

    override suspend fun connect(target: TunnelTarget, connectTimeoutMillis: Int): Result<PipeEndpoint> =
        when (target) {
            is TunnelTarget.NetworkHost -> network.connect(target, connectTimeoutMillis)
            is TunnelTarget.Device -> device.connect(target)
        }
}
