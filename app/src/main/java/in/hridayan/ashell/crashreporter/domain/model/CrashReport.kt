package `in`.hridayan.ashell.crashreporter.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CrashReport(
    val timestamp: Long,
    val deviceName: String,
    val manufacturer: String,
    val osVersion: String,
    val stackTrace: String
)