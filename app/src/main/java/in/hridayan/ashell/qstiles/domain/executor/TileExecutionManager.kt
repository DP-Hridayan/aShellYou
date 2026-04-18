package `in`.hridayan.ashell.qstiles.domain.executor

import `in`.hridayan.ashell.qstiles.data.provider.TileNotificationHelper
import `in`.hridayan.ashell.qstiles.domain.model.RunningTileState
import `in`.hridayan.ashell.qstiles.domain.model.TileActiveState
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.model.TileErrorType
import `in`.hridayan.ashell.qstiles.domain.model.TileExecutionMode
import `in`.hridayan.ashell.qstiles.domain.model.TileLog
import `in`.hridayan.ashell.qstiles.domain.repository.TileLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton manager that owns all tile execution state.
 *
 * Concurrency policy:
 *  - Same tile: cannot run concurrently (duplicate clicks are ignored).
 *  - Different tiles: fully parallel (each tile has its own Job).
 *
 * Running state is volatile — it resets to empty on process restart.
 */
@Singleton
class TileExecutionManager @Inject constructor(
    private val shizukuExecutor: CommandExecutor,
    private val rootExecutor: CommandExecutor,
    private val logRepository: TileLogRepository,
    private val notificationHelper: TileNotificationHelper
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Maps tileId → active Job. Used to prevent duplicate execution. */
    private val runningJobs = ConcurrentHashMap<Int, Job>()

    private val _runningTileStates = MutableStateFlow<Map<Int, RunningTileState>>(emptyMap())

    /** UI-observable running state: maps tileId → RunningTileState(tileId, startTime). */
    val runningTileStates: StateFlow<Map<Int, RunningTileState>> = _runningTileStates.asStateFlow()

    /**
     * Returns whether the tile with [tileId] is currently executing.
     */
    fun isTileRunning(tileId: Int): Boolean = runningJobs.containsKey(tileId)

    /**
     * Triggers execution of [TileActiveState.commandToExecute] in a new coroutine.
     *
     * Lifecycle:
     * 1. Check if already running → ignore.
     * 2. Add to running state.
     * 3. Execute via the appropriate executor.
     * 4. Log the result.
     * 5. Notify on failure.
     * 6. Cleanup from running state.
     */
    fun execute(tile: TileConfig) {
        if (isTileRunning(tile.id)) return

        val job = scope.launch {
            val startTime = System.currentTimeMillis()
            _runningTileStates.update { it + (tile.id to RunningTileState(tile.id, startTime)) }

            val result: CommandResult? = if (tile.timeoutMs != null) {
                withTimeoutOrNull(tile.timeoutMs) {
                    runExecutor(tile)
                }
            } else {
                runExecutor(tile)
            }

            val finalResult = result ?: CommandResult(
                output = "Command timed out after ${tile.timeoutMs}ms.",
                isSuccess = false,
                errorType = TileErrorType.TIMEOUT,
                durationMs = System.currentTimeMillis() - startTime
            )

            logRepository.insert(
                TileLog(
                    id = 0,
                    tileId = tile.id,
                    command = tile.activeState.commandToExecute,
                    output = finalResult.output,
                    isSuccess = finalResult.isSuccess,
                    executionMode = tile.executionMode,
                    timestamp = startTime,
                    durationMs = finalResult.durationMs,
                    errorType = finalResult.errorType.code
                )
            )

            if (!finalResult.isSuccess) {
                notificationHelper.notifyFailure(
                    tileName = tile.name,
                    errorType = finalResult.errorType,
                    errorMessage = finalResult.output,
                    notificationId = tile.id
                )
            }

            runningJobs.remove(tile.id)
            _runningTileStates.update { it - tile.id }
        }

        runningJobs[tile.id] = job

        job.invokeOnCompletion {
            runningJobs.remove(tile.id)
            _runningTileStates.update { it - tile.id }
        }
    }

    private suspend fun runExecutor(tile: TileConfig): CommandResult {
        val executor = when (tile.executionMode) {
            TileExecutionMode.SHIZUKU -> shizukuExecutor
            TileExecutionMode.ROOT -> rootExecutor
            else -> return CommandResult(
                output = "Unknown execution mode: ${tile.executionMode}",
                isSuccess = false,
                errorType = TileErrorType.UNKNOWN,
                durationMs = 0L
            )
        }
        return try {
            executor.execute(tile.activeState.commandToExecute)
        } catch (e: Exception) {
            CommandResult(
                output = "Unexpected error: ${e.message}",
                isSuccess = false,
                errorType = TileErrorType.UNKNOWN,
                durationMs = 0L
            )
        }
    }
}
