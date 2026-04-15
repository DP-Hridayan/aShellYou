package `in`.hridayan.ashell.qstiles.domain.usecase

import `in`.hridayan.ashell.qstiles.domain.model.TileConfig
import `in`.hridayan.ashell.qstiles.domain.model.TileLog
import `in`.hridayan.ashell.qstiles.domain.repository.TileLogRepository
import javax.inject.Inject

class ExecuteTileUseCase @Inject constructor(
    private val logRepository: TileLogRepository
) {

    suspend operator fun invoke(
        tile: TileConfig,
        output: String,
        success: Boolean
    ) {
        val log = TileLog(
            id = System.currentTimeMillis(),
            tileId = tile.id,
            command = tile.command,
            output = output,
            isSuccess = success,
            executionMode = tile.executionMode,
            timestamp = System.currentTimeMillis()
        )

        logRepository.insert(log)
    }
}