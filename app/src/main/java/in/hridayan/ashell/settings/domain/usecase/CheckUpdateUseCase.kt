package `in`.hridayan.ashell.settings.domain.usecase

import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import `in`.hridayan.ashell.settings.domain.repository.UpdateRepository
import javax.inject.Inject

class CheckUpdateUseCase @Inject constructor(
    private val repository: UpdateRepository
) {
    suspend operator fun invoke(currentVersion: String, includePrerelease: Boolean): UpdateResult {
        return when (val result = repository.fetchLatestRelease(includePrerelease)) {
            is UpdateResult.Success -> {
                val isNewer = isNewerVersion(result.release.tagName, currentVersion)
                UpdateResult.Success(result.release, isNewer)
            }

            is UpdateResult.NetworkError -> UpdateResult.NetworkError
            is UpdateResult.Timeout -> UpdateResult.Timeout
            is UpdateResult.UnknownError -> UpdateResult.UnknownError
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.trimStart('v').split(".")
        val currentParts = current.removeSuffix("-debug").trimStart('v').split(".")

        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val l = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
            val c = currentParts.getOrNull(i)?.toIntOrNull() ?: 0
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}

