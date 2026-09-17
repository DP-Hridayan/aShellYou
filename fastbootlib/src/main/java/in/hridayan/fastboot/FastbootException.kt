package `in`.hridayan.fastboot

/**
 * Thrown when fastboot communication fails, is aborted, or the device answers with something the
 * protocol does not allow. The message is technical detail for logs and diagnostics, never text
 * shown to the user on its own.
 */
class FastbootException(message: String, cause: Throwable? = null) : Exception(message, cause)
