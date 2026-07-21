package `in`.hridayan.ashell.settings.domain.usecase

import `in`.hridayan.ashell.settings.domain.model.DownloadState
import `in`.hridayan.ashell.settings.domain.repository.DownloadRepository
import javax.inject.Inject

class DownloadApkUseCase @Inject constructor(
    private val repo: DownloadRepository
) {
    suspend operator fun invoke(
        url: String,
        fileName: String,
        onProgress: (DownloadState) -> Unit
    ) {
        repo.downloadApk(url, fileName, onProgress)
    }

    fun cancel() {
        repo.cancelDownload()
    }
}
