package `in`.hridayan.ashell.shell.wifi_adb_shell.service

/**
 * What a caller should do in response to a stop request.
 */
enum class ServiceStopAction {
    /** The service reached the foreground, so it is safe to stop from outside. */
    StopService,

    /** A start is still in flight; the stop is recorded and applied once the start lands. */
    AwaitStart,

    /** Nothing was started, so there is nothing to stop. */
    Ignore
}

/**
 * Keeps a foreground service's start and stop requests in a safe order.
 *
 * Android requires that every delivery of `onStartCommand` that follows a `startForegroundService`
 * call reaches `startForeground`, or it kills the app with
 * `ForegroundServiceDidNotStartInTimeException`. Stopping from outside in the window between the two
 * breaks that contract, because the service is destroyed before it ever runs.
 *
 * This gate closes that window. A stop arriving too early is recorded rather than applied, and the
 * service consumes it by stopping itself right after it has entered the foreground.
 *
 * All state is guarded because starts and stops arrive from background threads while the service
 * callbacks run on the main thread.
 */
class ForegroundServiceGate {

    private var startRequested = false
    private var foregroundStarted = false
    private var pendingStop = false

    val isForegroundStarted: Boolean
        @Synchronized get() = foregroundStarted

    /**
     * @return true when the caller should actually start the service, false when a start is already
     * in flight or the service is already running.
     */
    @Synchronized
    fun onStartRequested(): Boolean {
        pendingStop = false
        if (startRequested) return false
        startRequested = true
        return true
    }

    /**
     * @return true when the service should stop itself immediately, because a stop arrived while its
     * start was still in flight.
     */
    @Synchronized
    fun onForegroundStarted(): Boolean {
        startRequested = true
        foregroundStarted = true
        if (!pendingStop) return false
        reset()
        return true
    }

    @Synchronized
    fun onStopRequested(): ServiceStopAction = when {
        !startRequested -> ServiceStopAction.Ignore

        foregroundStarted -> {
            reset()
            ServiceStopAction.StopService
        }

        else -> {
            pendingStop = true
            ServiceStopAction.AwaitStart
        }
    }

    @Synchronized
    fun onDestroyed() = reset()

    private fun reset() {
        startRequested = false
        foregroundStarted = false
        pendingStop = false
    }
}
