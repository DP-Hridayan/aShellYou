package `in`.hridayan.ashell.qstiles.data.repository

import `in`.hridayan.ashell.qstiles.data.dao.TileLogDao
import `in`.hridayan.ashell.qstiles.data.mapper.toDomain
import `in`.hridayan.ashell.qstiles.data.model.TileLogEntity
import `in`.hridayan.ashell.qstiles.domain.model.TileLog
import `in`.hridayan.ashell.qstiles.domain.repository.TileLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TileLogRepositoryImpl @Inject constructor(
    private val dao: TileLogDao
) : TileLogRepository {

    override suspend fun insert(log: TileLog) {
        dao.insert(
            TileLogEntity(
                tileId = log.tileId,
                command = log.command,
                output = log.output,
                isSuccess = log.isSuccess,
                executionMode = log.executionMode,
                timestamp = log.timestamp,
                durationMs = log.durationMs,
                errorType = log.errorType
            )
        )
    }

    override fun getLogs(tileId: Int): Flow<List<TileLog>> {
        return dao.getLogsForTile(tileId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getAllLogs(): Flow<List<TileLog>> {
        return dao.getAllLogs().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getTotalExecutions() = dao.getTotalExecutions()

    override fun getSuccessCount() = dao.getSuccessCount()
}