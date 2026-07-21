package `in`.hridayan.ashell.logcat.domain.model

data class LogEntry(
    val id: Long,
    val timestamp: String,
    val pid: String,
    val tid: String,
    val uid: String,
    val packageName: String,
    val level: LogLevel,
    val tag: String,
    val message: String,
)
