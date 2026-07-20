package `in`.hridayan.ashell.settings.domain.repository

import `in`.hridayan.ashell.settings.domain.model.DownloadState

interface DownloadRepository {
    suspend fun downloadApk(
        url: String,
        fileName: String,
        onProgress: (DownloadState) -> Unit
    )

    fun cancelDownload()
}
