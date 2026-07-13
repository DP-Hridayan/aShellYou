package `in`.hridayan.fastboot

/**
 * Represents a response from a fastboot device.
 * Fastboot responses start with a 4-byte status code followed by optional data.
 */
data class FastbootResponse(
    val status: ResponseStatus,
    val data: String
) {
    /** Whether the command completed successfully. */
    val isOkay: Boolean get() = status == ResponseStatus.OKAY

    /** Whether the command failed. */
    val isFail: Boolean get() = status == ResponseStatus.FAIL
}

/**
 * Fastboot protocol response status codes.
 * Each response from the device starts with one of these 4-byte ASCII prefixes.
 */
enum class ResponseStatus(val prefix: String) {
    /** Command completed successfully. */
    OKAY("OKAY"),
    /** Command failed. Remaining bytes are error message. */
    FAIL("FAIL"),
    /** Device is ready to receive data. Remaining bytes are hex-encoded data size. */
    DATA("DATA"),
    /** Informational message. Remaining bytes are text. */
    INFO("INFO");

    companion object {
        fun fromPrefix(prefix: String): ResponseStatus? =
            entries.firstOrNull { it.prefix == prefix }
    }
}
