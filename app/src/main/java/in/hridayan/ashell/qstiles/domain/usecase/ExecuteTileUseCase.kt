package `in`.hridayan.ashell.qstiles.domain.usecase

import `in`.hridayan.ashell.qstiles.domain.executor.CommandResult
import `in`.hridayan.ashell.qstiles.domain.executor.TileExecutionManager
import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.model.TileLog
import `in`.hridayan.ashell.qstiles.domain.repository.TileLogRepository
import javax.inject.Inject

/**
 * Use-case kept for backward-compatibility but no longer used by [TileExecutionManager],
 * which handles logging internally as part of the unified execution lifecycle.
 */
class ExecuteTileUseCase @Inject constructor(
    private val logRepository: TileLogRepository
) {

    suspend operator fun invoke(
        tile: TileConfig,
        result: CommandResult
    ) {
        val log = TileLog(
            id = 0,
            tileId = tile.id,
            command = tile.activeState.commandToExecute,
            output = result.output,
            isSuccess = result.isSuccess,
            executionMode = tile.executionMode,
            timestamp = System.currentTimeMillis(),
            durationMs = result.durationMs,
            errorType = result.errorType.code
        )
        logRepository.insert(log)
    }
}