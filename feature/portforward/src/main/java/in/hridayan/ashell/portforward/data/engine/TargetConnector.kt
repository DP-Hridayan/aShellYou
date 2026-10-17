package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget

/**
 * Opens a connection to a tunnel's target for one accepted client.
 *
 * A failure carries its [in.hridayan.ashell.portforward.domain.model.TunnelError] in a
 * [in.hridayan.ashell.portforward.domain.model.TunnelException].
 */
interface TargetConnector {
    suspend fun connect(target: TunnelTarget, connectTimeoutMillis: Int): Result<PipeEndpoint>
}
