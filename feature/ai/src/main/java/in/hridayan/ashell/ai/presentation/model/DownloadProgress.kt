package `in`.hridayan.ashell.ai.presentation.model

/**
 * Represents the progress of a model download.
 */
sealed interface DownloadProgress {
    data object Idle : DownloadProgress
    data class Downloading(val bytesDownloaded: Long, val totalBytes: Long) : DownloadProgress {
        val progressFraction: Float
            get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
        val progressPercent: Int
            get() = (progressFraction * 100).toInt()
    }
    data object Completed : DownloadProgress
    data class Failed(val error: String) : DownloadProgress
}
