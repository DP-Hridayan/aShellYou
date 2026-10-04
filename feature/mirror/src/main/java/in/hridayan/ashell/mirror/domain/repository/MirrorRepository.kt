package `in`.hridayan.ashell.mirror.domain.repository

import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.MirrorOptions
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import `in`.hridayan.ashell.mirror.domain.model.VideoOutput
import `in`.hridayan.ashell.mirror.domain.quality.TargetDevice
import kotlinx.coroutines.flow.StateFlow

/**
 * Runs one screen-mirroring session at a time against another device.
 *
 * A session lives exactly as long as the [run] call: cancelling the caller stops the server and
 * closes every stream. Failures are reported through [state], never thrown.
 */
interface MirrorRepository {

    val state: StateFlow<MirrorState>

    /** The last second of the stream, or null while nothing is streaming. */
    val stats: StateFlow<StreamStats?>

    /**
     * Starts the server and streams until the session ends, fails, or the caller is cancelled.
     *
     * [options] is asked for each time a server is about to start, with what the device reported
     * about its screen while it was being prepared.
     */
    suspend fun run(transport: ExternalDeviceTransport, options: suspend (TargetDevice) -> MirrorOptions)

    /** Queues [message] for the device. Ignored while no session is streaming. */
    fun send(message: ControlMessage)

    /**
     * Sets where decoded frames are drawn, or null when there is nowhere to draw. Video keeps being
     * received and discarded without an output, and a new output asks the device for a fresh key
     * frame.
     */
    fun setVideoOutput(output: VideoOutput?)
}
