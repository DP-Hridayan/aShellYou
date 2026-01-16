package `in`.hridayan.ashell.core.domain.model

import android.net.Uri

/**
 * Represents the progress and result of a file save operation.
 */
sealed class SaveProgress {
    /** Save operation is in progress */
    data class Saving(
        val currentLine: Int,
        val totalLines: Int
    ) : SaveProgress() {
        val progress: Float
            get() = if (totalLines > 0) currentLine.toFloat() / totalLines else 0f
    }

    /** Save operation completed successfully */
    data class Success(val uri: Uri?) : SaveProgress()

    /** Save operation failed */
    data class Error(val message: String) : SaveProgress()

    /** No save operation in progress (idle state) */
    data object Idle : SaveProgress()
}
