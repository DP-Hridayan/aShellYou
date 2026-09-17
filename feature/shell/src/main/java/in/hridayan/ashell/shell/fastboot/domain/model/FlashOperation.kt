package `in`.hridayan.ashell.shell.fastboot.domain.model

/**
 * State of an ongoing flash, erase or boot operation.
 *
 * [DOWNLOADING] and [WRITING] are deliberately distinct: during the first the device is only
 * filling a memory buffer, during the second it is committing to a partition and must not be
 * interrupted.
 */
data class FlashOperation(
    val partition: String = "",
    val fileName: String = "",
    val progress: Float = 0f,
    val status: FlashStatus = FlashStatus.IDLE,
    val bytesSent: Long = 0L,
    val totalBytes: Long = 0L,
    val error: FastbootError? = null,
    val errorDetail: String? = null,
)

enum class FlashStatus {
    IDLE,
    READING_FILE,
    DOWNLOADING,
    WRITING,
    ERASING,
    CANCELLING,
    COMPLETE,
    ERROR,
    CANCELLED;

    val isActive: Boolean
        get() = this == READING_FILE || this == DOWNLOADING || this == WRITING ||
            this == ERASING || this == CANCELLING

    val isFinished: Boolean
        get() = this == COMPLETE || this == ERROR || this == CANCELLED
}
