package `in`.hridayan.ashell.crashreporter.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CrashReport(
    val timestamp: Long,
    val deviceBrand: String,
    val deviceModel: String,
    val manufacturer: String,
    val osVersion: String,
    val cpuAbi: String,
    val socManufacturer: String,
    val appPackageName: String,
    val appVersionName: String,
    val appVersionCode: String,
    val stackTrace: String
)