package `in`.hridayan.ashell.shell.fastboot.domain.model

/**
 * Represents the state of an ongoing flash/erase operation.
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
    ERROR
}
