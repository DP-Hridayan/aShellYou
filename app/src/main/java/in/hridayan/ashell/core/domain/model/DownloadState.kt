package `in`.hridayan.ashell.core.domain.model

import java.io.File

sealed class DownloadState {
    object Idle : DownloadState()
    object Started : DownloadState()
    data class Progress(val percent: Float) : DownloadState()
    data class Success(val file: File) : DownloadState()
    data class Error(val message: String) : DownloadState()
    object Cancelled : DownloadState()
}
