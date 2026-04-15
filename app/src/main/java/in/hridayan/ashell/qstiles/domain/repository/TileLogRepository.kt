package `in`.hridayan.ashell.qstiles.domain.repository

import `in`.hridayan.ashell.qstiles.domain.model.TileLog
import kotlinx.coroutines.flow.Flow

interface TileLogRepository {
    suspend fun insert(log: TileLog)
    fun getLogs(tileId: Int): Flow<List<TileLog>>
    fun getAllLogs(): Flow<List<TileLog>>
    fun getTotalExecutions(): Flow<Int>
    fun getSuccessCount(): Flow<Int>
}