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
    val progress: Float
        get() = if (totalBytes > 0) {
            (bytesTransferred.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
        } else 0f
    
    val isComplete: Boolean
        get() = status == OperationStatus.COMPLETED || status == OperationStatus.FAILED
}
