package `in`.hridayan.ashell.shell.fastboot.domain.model

/**
 * Every failure a flash, erase or boot operation can report. The presentation layer maps each
 * value to a localized message; the data and domain layers never carry user facing text.
 */
enum class FastbootError {
    NO_DEVICE_CONNECTED,
    FILE_OPEN_FAILED,
    FILE_READ_FAILED,
    IMAGE_TOO_LARGE,
    DEVICE_REJECTED,
    TRANSFER_FAILED,
    CONNECTION_LOST,
    UNKNOWN,
}
