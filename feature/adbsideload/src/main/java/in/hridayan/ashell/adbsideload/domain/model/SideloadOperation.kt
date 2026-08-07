package `in`.hridayan.ashell.adbsideload.domain.model

data class SideloadOperation(
    val fileName: String = "",
    val status: SideloadStatus = SideloadStatus.IDLE,
    val progress: Float = 0f,
    val bytesSent: Long = 0L,
    val totalBytes: Long = 0L,
    val transferRateMBps: Float = 0f,
    val currentBlock: Int = 0,
    val totalBlocks: Int = 0,
    val message: String = "",
)
