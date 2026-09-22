package `in`.hridayan.ashell.shell.file_browser.domain.model

import androidx.annotation.Keep
import java.util.UUID

/**
 * Status of a file operation
 */
@Keep
enum class OperationStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

/**
 * Represents an ongoing file operation (upload, download, copy, move)
 */
data class FileOperation(
    val id: String = UUID.randomUUID().toString(),
    val type: OperationType,
    val fileName: String,
    val bytesTransferred: Long = 0L,
    val totalBytes: Long = 0L,
    val message: String = "",
    val status: OperationStatus = OperationStatus.PENDING
) {
    /**
     * True when the transfer knows how big the file is.
     *
     * A metadata call can fail while the transfer itself works, and when that happened the bar showed
     * zero for the whole download. An unknown total is now shown as unknown rather than as no
     * progress at all.
     */
    val hasKnownTotal: Boolean get() = totalBytes > 0

    val progress: Float
        get() = if (hasKnownTotal) {
            (bytesTransferred.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val isComplete: Boolean
        get() = status == OperationStatus.COMPLETED || status == OperationStatus.FAILED
}
