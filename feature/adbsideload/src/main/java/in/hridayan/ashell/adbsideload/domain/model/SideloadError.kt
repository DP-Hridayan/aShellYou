package `in`.hridayan.ashell.adbsideload.domain.model

/**
 * Every failure the sideload feature can report. The presentation layer maps each value to a
 * localized message; the data and domain layers never carry user-facing text.
 */
enum class SideloadError {
    NO_DEVICE_CONNECTED,
    USB_SERVICE_UNAVAILABLE,
    NO_ADB_INTERFACE,
    USB_OPEN_FAILED,
    USB_CLAIM_FAILED,
    ADB_KEY_UNAVAILABLE,
    CONNECTION_TIMED_OUT,
    CONNECTION_FAILED,
    CONNECTION_LOST,
    FILE_SIZE_UNKNOWN,
    FILE_OPEN_FAILED,
    FILE_READ_FAILED,
    RECOVERY_NOT_RESPONDING,
    STREAM_OPEN_FAILED,
    STREAM_CLOSED,
    RECOVERY_REPORTED_FAILURE,
    BLOCK_OUT_OF_RANGE,
    INVALID_REQUEST,
    UNKNOWN,
}
