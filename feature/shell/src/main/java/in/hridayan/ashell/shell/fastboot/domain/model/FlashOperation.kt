package `in`.hridayan.ashell.shell.fastboot.domain.model

/**
 * Represents the state of an ongoing flash/erase/boot operation.
 */
data class FlashOperation(
    val partition: String = "",
    val fileName: String = "",
    val progress: Float = 0f,
    val status: FlashStatus = FlashStatus.IDLE,
    val message: String = ""
)

enum class FlashStatus {
    IDLE,
    READING_FILE,
    DOWNLOADING,
    FLASHING,
    ERASING,
    CANCELLING,
    COMPLETE,
    ERROR,
    CANCELLED;

    val isActive: Boolean
        get() = this == READING_FILE || this == DOWNLOADING || this == FLASHING ||
            this == ERASING || this == CANCELLING

    val isFinished: Boolean
        get() = this == COMPLETE || this == ERROR || this == CANCELLED
}
