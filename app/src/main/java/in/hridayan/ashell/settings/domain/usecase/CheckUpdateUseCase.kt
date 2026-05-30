package `in`.hridayan.ashell.settings.domain.usecase

import `in`.hridayan.ashell.core.domain.repository.GithubDataRepository
import `in`.hridayan.ashell.settings.domain.model.UpdateResult
import javax.inject.Inject

class CheckUpdateUseCase @Inject constructor(
    private val repository: GithubDataRepository
) {
    suspend operator fun invoke(
        currentVersion: String,
        includePrerelease: Boolean,
        releaseType: Int
    ): UpdateResult {
        return when (val result = repository.fetchLatestRelease(includePrerelease, releaseType)) {
            is UpdateResult.Success -> {
                val isNewer = isNewerVersion(result.release.tagName, currentVersion)
                UpdateResult.Success(result.release, isNewer)
            }

            is UpdateResult.NetworkError -> UpdateResult.NetworkError
            is UpdateResult.Timeout -> UpdateResult.Timeout
            is UpdateResult.UnknownError -> UpdateResult.UnknownError
        }
    }

    private data class ParsedVersion(
        val numbers: List<Int>,
        val stage: Int,
        val stageNumber: Int
    )

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val l = parseVersion(latest)
        val c = parseVersion(current)

        for (i in 0 until maxOf(l.numbers.size, c.numbers.size)) {
            val lv = l.numbers.getOrElse(i) { 0 }
            val cv = c.numbers.getOrElse(i) { 0 }

            if (lv > cv) return true
            if (lv < cv) return false
        }

        if (l.stage > c.stage) return true
        if (l.stage < c.stage) return false

        return l.stageNumber > c.stageNumber
    }

    private fun parseVersion(version: String): ParsedVersion {
        val cleaned = version.trim().trimStart('v')

        val regex = Regex("""^(\d+(?:\.\d+)*)(?:-(alpha|beta|debug)(\d+)?)?$""")
        val match = regex.matchEntire(cleaned)
            ?: return ParsedVersion(emptyList(), 0, 0)

        val numbers = match.groupValues[1]
            .split(".")
            .map { it.toInt() }

        val stage = when (match.groupValues[2]) {
            "debug" -> 0
            "alpha" -> 1
            "beta" -> 2
            else -> 3 // stable
        }

        val stageNumber = match.groupValues[3].toIntOrNull() ?: 0

        return ParsedVersion(numbers, stage, stageNumber)
    }
}

