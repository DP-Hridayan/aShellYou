package `in`.hridayan.ashell.crashreporter.data.repository

import `in`.hridayan.ashell.crashreporter.data.database.CrashLogDao
import `in`.hridayan.ashell.crashreporter.data.model.CrashLogEntity
import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CrashRepositoryImpl @Inject constructor(private val dao: CrashLogDao) : CrashRepository {
    override suspend fun addCrash(crash: CrashReport) {
        dao.insertCrash(
            CrashLogEntity(
                timestamp = crash.timestamp,
                deviceName = crash.deviceName,
                manufacturer = crash.manufacturer,
                osVersion = crash.osVersion,
                stackTrace = crash.stackTrace
            )
        )
    }

    override suspend fun getLatestCrash(): CrashReport? {
        return dao.getLatestCrash()
    }

    override fun getAllCrashes(): Flow<List<CrashReport>> {
        return dao.getAllCrashes().map { list ->
            list.map { entity ->
                CrashReport(
                    timestamp = entity.timestamp,
                    deviceName = entity.deviceName,
                    manufacturer = entity.manufacturer,
                    osVersion = entity.osVersion,
                    stackTrace = entity.stackTrace
                )
            }
        }
    }

    override suspend fun clearAllCrashes() {
        dao.clearAllCrashes()
    }
}
