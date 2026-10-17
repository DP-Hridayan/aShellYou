package `in`.hridayan.ashell.portforward.data.engine

import `in`.hridayan.ashell.core.common.domain.model.AdbDuplexStream
import `in`.hridayan.ashell.portforward.data.device.DeviceChannels
import `in`.hridayan.ashell.portforward.di.PortForwardDispatcher
import `in`.hridayan.ashell.portforward.domain.model.TunnelError
import `in`.hridayan.ashell.portforward.domain.model.TunnelException
import `in`.hridayan.ashell.portforward.domain.model.TunnelTarget
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeoutOrNull
import java.net.ConnectException
import javax.inject.Inject

/**
 * Opens an ADB stream to a socket on a connected device.
 *
 * The libraries report "nothing listening" and "connection lost" with the same exception, so the
 * channel's connection is checked afterwards to tell them apart.
 */
class DeviceTargetConnector @Inject constructor(
    private val channels: DeviceChannels,
    @param:PortForwardDispatcher private val dispatcher: CoroutineDispatcher
) {

    suspend fun connect(target: TunnelTarget.Device): Result<PipeEndpoint> = open(target).map(::AdbStreamEndpoint)

    /** Opens a raw stream to [target]'s socket. */
    suspend fun open(target: TunnelTarget.Device): Result<AdbDuplexStream> {
        if (!channels.isReachable(target)) return failure(TunnelError.DeviceNotConnected)
        val opened = withTimeoutOrNull(OPEN_TIMEOUT_MS) {
            channels[target.transport].openStream(target.endpoint.service, dispatcher)
        } ?: return failure(TunnelError.DeviceOpenTimeout)
        return opened.fold(onSuccess = { Result.success(it) }, onFailure = { failure(classify(target, it)) })
    }

    private fun classify(target: TunnelTarget.Device, error: Throwable): TunnelError = when {
        error is TimeoutCancellationException -> TunnelError.DeviceOpenTimeout
        !channels.isReachable(target) || !channels[target.transport].isConnected -> TunnelError.DeviceNotConnected
        error is ConnectException -> TunnelError.NothingListeningOnDevice
        else -> TunnelError.Unexpected(error.message)
    }

    private fun <T> failure(error: TunnelError): Result<T> = Result.failure(TunnelException(error))

    private companion object {
        const val OPEN_TIMEOUT_MS = 5_000L
    }
}
