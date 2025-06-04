package `in`.hridayan.ashell.core.domain.repository

import `in`.hridayan.ashell.core.domain.model.DownloadState

interface DownloadRepository {
    suspend fun downloadApk(
        url: String,
        fileName: String,
        onProgress: (DownloadState) -> Unit
    )

    fun cancelDownload()
}
