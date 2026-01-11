package `in`.hridayan.ashell.crashreporter.data.repository

import `in`.hridayan.ashell.crashreporter.data.database.CrashLogDao
import `in`.hridayan.ashell.crashreporter.data.model.CrashLogEntity
import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport
import `in`.hridayan.ashell.crashreporter.domain.repository.CrashRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import javax.inject.Inject

class CrashRepositoryImpl @Inject constructor(private val dao: CrashLogDao) : CrashRepository {
    override suspend fun addCrash(crash: CrashReport) {
        dao.insertCrash(
            CrashLogEntity(
                timestamp = crash.timestamp,
                deviceBrand = crash.deviceBrand,
                deviceModel = crash.deviceModel,
                manufacturer = crash.manufacturer,
                osVersion = crash.osVersion,
                socManufacturer = crash.socManufacturer,
                cpuAbi = crash.cpuAbi,
                appPackageName = crash.appPackageName,
                appVersionName = crash.appVersionName,
                appVersionCode = crash.appVersionCode,
                stackTrace = crash.stackTrace
            )
        )
    }

    override fun getCrashById(id: Long): Flow<CrashReport?> {
        return dao.getCrashById(id)
    }

    override suspend fun getLatestCrash(): CrashReport? {
        return dao.getLatestCrash()
    }

    override fun getAllCrashes(): Flow<List<CrashReport>> {
        return dao.getAllCrashes().map { list ->
            list.map { crash ->
                CrashReport(
                    id = crash.id,
                    timestamp = crash.timestamp,
                    deviceBrand = crash.deviceBrand,
                    deviceModel = crash.deviceModel,
                    manufacturer = crash.manufacturer,
                    osVersion = crash.osVersion,
                    socManufacturer = crash.socManufacturer,
                    cpuAbi = crash.cpuAbi,
                    appPackageName = crash.appPackageName,
                    appVersionName = crash.appVersionName,
                    appVersionCode = crash.appVersionCode,
                    stackTrace = crash.stackTrace
                )
            }
        }
    }

    override suspend fun clearAllCrashes() {
        dao.clearAllCrashes()
    }
}
