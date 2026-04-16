package `in`.hridayan.ashell.qstiles.data.mapper

import `in`.hridayan.ashell.qstiles.data.model.TileLogEntity
import `in`.hridayan.ashell.qstiles.domain.model.TileLog

fun TileLogEntity.toDomain(): TileLog {
    return TileLog(
        id = id,
        tileId = tileId,
        command = command,
        output = output,
        isSuccess = isSuccess,
        executionMode = executionMode,
        timestamp = timestamp,
        durationMs = durationMs,
        errorType = errorType
    )
}