package `in`.hridayan.ashell.crashreporter.domain.model

data class CrashReport(
    val timestamp: Long,
    val deviceName: String,
    val manufacturer: String,
    val osVersion: String,
    val crashLog: String
)