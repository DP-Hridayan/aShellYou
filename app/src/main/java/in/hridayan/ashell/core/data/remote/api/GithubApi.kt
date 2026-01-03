package `in`.hridayan.ashell.core.data.remote.api

import `in`.hridayan.ashell.core.data.remote.dto.GithubReleaseDto
import `in`.hridayan.ashell.core.data.remote.dto.GithubRepoStatsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class GithubApi @Inject constructor(
    private val client: HttpClient
) {
    private val baseRepoUrl = "https://api.github.com/repos/DP-Hridayan/aShellYou"

    private val releasesUrl = "$baseRepoUrl/releases"

    private val latestReleaseUrl = "$baseRepoUrl/releases/latest"

    suspend fun fetchLatestRelease(includePrerelease: Boolean): GithubReleaseDto {
        return if (includePrerelease) {
            val allReleases: List<GithubReleaseDto> = client.get(releasesUrl).body()
            allReleases.first()
        } else {
            client.get(latestReleaseUrl).body()
        }
    }

    suspend fun fetchAllReleases(): List<GithubReleaseDto> {
        return client.get(releasesUrl).body()
    }

    suspend fun fetchRepoStats(): GithubRepoStatsDto {
        return client.get(baseRepoUrl).body()
    }
}
