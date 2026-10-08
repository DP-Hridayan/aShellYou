package `in`.hridayan.ashell.mirror.data.repository

import android.os.SystemClock
import android.util.Log
import dagger.hilt.android.scopes.ViewModelScoped
import `in`.hridayan.ashell.core.common.data.provider.DispatcherProvider
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannel
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceChannelNames
import `in`.hridayan.ashell.core.common.domain.model.ExternalDeviceTransport
import `in`.hridayan.ashell.mirror.data.connection.ScrcpySocketConnector
import `in`.hridayan.ashell.mirror.data.connection.ScrcpySockets
import `in`.hridayan.ashell.mirror.data.server.ScrcpyServerDeployer
import `in`.hridayan.ashell.mirror.data.server.ServerProcess
import `in`.hridayan.ashell.mirror.data.server.awaitReconnect
import `in`.hridayan.ashell.mirror.data.server.runQuietly
import `in`.hridayan.ashell.mirror.data.session.LowLatencyWifiLock
import `in`.hridayan.ashell.mirror.data.session.MirrorStreamer
import `in`.hridayan.ashell.mirror.domain.model.ControlMessage
import `in`.hridayan.ashell.mirror.domain.model.EndReason
import `in`.hridayan.ashell.mirror.domain.model.MirrorError
import `in`.hridayan.ashell.mirror.domain.model.MirrorOptions
import `in`.hridayan.ashell.mirror.domain.model.MirrorState
import `in`.hridayan.ashell.mirror.domain.model.StartStep
import `in`.hridayan.ashell.mirror.domain.model.StreamStats
import `in`.hridayan.ashell.mirror.domain.model.VideoOutput
import `in`.hridayan.ashell.mirror.domain.protocol.ScidGenerator
import `in`.hridayan.ashell.mirror.domain.protocol.ServerCommandBuilder
import `in`.hridayan.ashell.mirror.domain.protocol.ServerLogSignal
import `in`.hridayan.ashell.mirror.domain.protocol.TargetProbeParser
import `in`.hridayan.ashell.mirror.domain.quality.TargetDevice
import `in`.hridayan.ashell.mirror.domain.repository.MirrorRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Named

private const val SHELL_SERVICE = "shell:"
private const val MAX_RECONNECT_ATTEMPTS = 3
private const val RECONNECT_POLLS = 10
private const val RECONNECT_POLL_INTERVAL_MS = 500L
private const val SERVER_EXIT_GRACE_MS = 500L
private const val STOP_TIMEOUT_MS = 5_000L
private const val DEPLOY_TIMEOUT_MS = 20_000L
private const val TAG = "MirrorSession"
private const val ERROR_LOG_LINES = 4
private const val SERVER_ERROR_MARKER = "ERROR:"
private const val SWEEP_KILL_MARKER = "killed"

/**
 * Runs scrcpy sessions over an [ExternalDeviceChannel] and turns everything that happens into
 * [MirrorState]: deploy the server, launch it, connect to its sockets, stream, then decide why the
 * session ended.
 *
 * One instance belongs to one mirror screen, so state never leaks between screens. A session that
 * loses its transport waits for it to come back a few times before reporting the device gone.
 */
@ViewModelScoped
class MirrorRepositoryImpl @Inject constructor(
    @param:Named(ExternalDeviceChannelNames.OTG) private val otgChannel: ExternalDeviceChannel,
    @param:Named(ExternalDeviceChannelNames.WIFI_ADB) private val wifiAdbChannel: ExternalDeviceChannel,
    private val deployer: ScrcpyServerDeployer,
    private val commandBuilder: ServerCommandBuilder,
    private val connector: ScrcpySocketConnector,
    private val scidGenerator: ScidGenerator,
    private val wifiLock: LowLatencyWifiLock,
    dispatchers: DispatcherProvider
) : MirrorRepository {

    private enum class Outcome { FINISHED, TRANSPORT_LOST }

    /** A server that was started, or the outcome that prevented it. */
    private sealed interface Launch {
        class Started(val server: ServerProcess, val scid: Int) : Launch
        class Stopped(val outcome: Outcome) : Launch
    }

    /** Server log signals seen during one session; written by the log reader, read on exit. */
    private class Signals {
        @Volatile
        var versionMismatch = false

        @Volatile
        var encoderFailed = false
    }

    private val ioDispatcher = dispatchers.io
    private val streamer = MirrorStreamer(ioDispatcher)

    private val _state = MutableStateFlow<MirrorState>(MirrorState.Idle)
    override val state: StateFlow<MirrorState> = _state.asStateFlow()

    private val _stats = MutableStateFlow<StreamStats?>(null)
    override val stats: StateFlow<StreamStats?> = _stats.asStateFlow()

    override fun send(message: ControlMessage) = streamer.send(message)

    override fun setVideoOutput(output: VideoOutput?) = streamer.setVideoOutput(output)

    override suspend fun run(transport: ExternalDeviceTransport, options: suspend (TargetDevice) -> MirrorOptions) {
        try {
            when (transport) {
                ExternalDeviceTransport.OTG -> runWithReconnects(otgChannel, options)
                ExternalDeviceTransport.WIFI_ADB -> wifiLock.holdWhile {
                    runWithReconnects(wifiAdbChannel, options)
                }
            }
        } catch (e: CancellationException) {
            endOnCancellation(e)
        }
    }

    /**
     * A timeout deep inside a call is also a [CancellationException], but only a cancelled caller
     * means the user left. Any other cancellation is reported as a failure instead of leaving the
     * screen on its start-up spinner with nothing running.
     */
    private suspend fun endOnCancellation(e: CancellationException) {
        if (currentCoroutineContext().isActive) {
            _state.value = MirrorState.Failed(MirrorError.DeviceNotResponding)
            return
        }
        _state.value = MirrorState.Idle
        throw e
    }

    private suspend fun runWithReconnects(
        channel: ExternalDeviceChannel,
        options: suspend (TargetDevice) -> MirrorOptions
    ) {
        var reconnects = 0
        while (runSession(channel, options) == Outcome.TRANSPORT_LOST) {
            _state.value = MirrorState.Reconnecting
            val gaveUp = reconnects++ >= MAX_RECONNECT_ATTEMPTS
            if (gaveUp || !channel.awaitReconnect(RECONNECT_POLLS, RECONNECT_POLL_INTERVAL_MS)) {
                _state.value = MirrorState.Ended(EndReason.DEVICE_DISCONNECTED)
                return
            }
        }
    }

    private suspend fun runSession(
        channel: ExternalDeviceChannel,
        options: suspend (TargetDevice) -> MirrorOptions
    ): Outcome =
        when (val launch = launchServer(channel, options)) {
            is Launch.Stopped -> launch.outcome
            is Launch.Started -> try {
                superviseServer(channel, launch.server, launch.scid)
            } finally {
                val stoppingAt = SystemClock.elapsedRealtime()
                launch.server.stop()
                launch.server.pid?.let { pid ->
                    withContext(NonCancellable) {
                        channel.runQuietly(ServerCommandBuilder.stopCommand(pid), STOP_TIMEOUT_MS)
                    }
                }
                Log.i(TAG, "Teardown took ${SystemClock.elapsedRealtime() - stoppingAt} ms, pid=${launch.server.pid}")
            }
        }

    private suspend fun launchServer(
        channel: ExternalDeviceChannel,
        options: suspend (TargetDevice) -> MirrorOptions
    ): Launch {
        if (!channel.isConnected) return Launch.Stopped(fail(channel, MirrorError.NotConnected))

        _state.value = MirrorState.Starting(StartStep.PREPARING)
        val deployStartedAt = SystemClock.elapsedRealtime()
        val deployed = withTimeoutOrNull(DEPLOY_TIMEOUT_MS) {
            deployer.deploy(channel).onFailure { Log.w(TAG, "Deploying the server failed", it) }.getOrNull()
        } ?: return Launch.Stopped(fail(channel, MirrorError.DeployFailed))
        Log.i(TAG, "Deploy took ${SystemClock.elapsedRealtime() - deployStartedAt} ms, skipped=${deployed.skipped}")

        _state.value = MirrorState.Starting(StartStep.STARTING_SERVER, deployed.skipped)
        val prepared = mutableListOf<String>()
        channel.runQuietly(ServerCommandBuilder.prepareCommand(), STOP_TIMEOUT_MS) { line ->
            prepared += line
            if (line.startsWith(SWEEP_KILL_MARKER)) Log.i(TAG, "Leftover server sweep: $line")
        }
        val target = TargetProbeParser.parse(prepared)
        Log.i(TAG, "Target: $target")
        return startServer(channel, options(target), deployed.skipped)
    }

    private suspend fun startServer(
        channel: ExternalDeviceChannel,
        options: MirrorOptions,
        serverAlreadyDeployed: Boolean
    ): Launch {
        val scid = scidGenerator.next()
        val command = commandBuilder.build(scid, options).getOrNull()
            ?: return Launch.Stopped(fail(channel, MirrorError.StreamError))
        val server = channel.openStream(SHELL_SERVICE + command).getOrNull()
            ?: return Launch.Stopped(fail(channel, MirrorError.ServerExited(emptyList())))

        _state.value = MirrorState.Starting(StartStep.CONNECTING, serverAlreadyDeployed)
        return Launch.Started(ServerProcess(server), scid)
    }

    /** Reads the server's log for the whole session while connecting to it and streaming. */
    private suspend fun superviseServer(
        channel: ExternalDeviceChannel,
        server: ServerProcess,
        scid: Int
    ): Outcome = coroutineScope {
        val signals = Signals()
        val serverExited = CompletableDeferred<Unit>()
        val logReader = launch(ioDispatcher) {
            server.pumpLogs { signal -> onServerSignal(signal, signals) }
            serverExited.complete(Unit)
        }

        try {
            val sockets = connector.connectUnless(serverExited, channel, scid)
            if (sockets == null) {
                endFromServer(channel, server, signals, connecting = true)
            } else {
                streamThenEnd(channel, sockets, server, signals, serverExited)
            }
        } finally {
            logReader.cancel()
        }
    }

    private suspend fun streamThenEnd(
        channel: ExternalDeviceChannel,
        sockets: ScrcpySockets,
        server: ServerProcess,
        signals: Signals,
        serverExited: CompletableDeferred<Unit>
    ): Outcome {
        val error = try {
            streamer.stream(
                sockets = sockets,
                onDeviceName = { name ->
                    _state.value = MirrorState.Streaming(name, videoSize = null, isControlAvailable = true)
                },
                onSession = { size -> updateStreaming { it.copy(videoSize = size) } },
                onStats = { _stats.value = it }
            )
        } finally {
            _stats.value = null
            sockets.close()
        }

        if (error != null) return fail(channel, error)
        withTimeoutOrNull(SERVER_EXIT_GRACE_MS) { serverExited.await() }
        return endFromServer(channel, server, signals, connecting = false)
    }

    private fun onServerSignal(signal: ServerLogSignal, signals: Signals) {
        when (signal) {
            ServerLogSignal.INJECTION_DENIED -> {
                streamer.disableInput()
                updateStreaming { it.copy(isControlAvailable = false) }
            }

            ServerLogSignal.VERSION_MISMATCH -> signals.versionMismatch = true
            ServerLogSignal.ENCODER_FAILED -> signals.encoderFailed = true
        }
    }

    private fun endFromServer(
        channel: ExternalDeviceChannel,
        server: ServerProcess,
        signals: Signals,
        connecting: Boolean
    ): Outcome {
        if (!channel.isConnected) return Outcome.TRANSPORT_LOST

        val log = server.diagnosticLines(ERROR_LOG_LINES)
        val error = when {
            signals.versionMismatch -> MirrorError.ServerVersionMismatch
            signals.encoderFailed -> MirrorError.EncoderFailed(log)
            connecting -> MirrorError.ConnectTimeout(log)
            log.any { SERVER_ERROR_MARKER in it } -> MirrorError.ServerExited(log)
            else -> null
        }
        _state.value = error?.let { MirrorState.Failed(it) } ?: MirrorState.Ended(EndReason.SERVER_STOPPED)
        return Outcome.FINISHED
    }

    private fun fail(channel: ExternalDeviceChannel, error: MirrorError): Outcome {
        if (!channel.isConnected && error != MirrorError.NotConnected) return Outcome.TRANSPORT_LOST
        _state.value = MirrorState.Failed(error)
        return Outcome.FINISHED
    }

    private fun updateStreaming(transform: (MirrorState.Streaming) -> MirrorState.Streaming) {
        _state.update { current -> if (current is MirrorState.Streaming) transform(current) else current }
    }
}
