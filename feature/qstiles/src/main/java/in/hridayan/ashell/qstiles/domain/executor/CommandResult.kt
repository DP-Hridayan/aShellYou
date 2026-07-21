package `in`.hridayan.ashell.qstiles.domain.executor

import `in`.hridayan.ashell.qstiles.domain.model.TileErrorType

data class CommandResult(
    val output: String,
    val isSuccess: Boolean,
    val errorType: TileErrorType,
    val durationMs: Long
)
