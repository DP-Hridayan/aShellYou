package `in`.hridayan.ashell.core.data.remote.mapper

import `in`.hridayan.ashell.core.data.remote.dto.GithubReleaseDto
import `in`.hridayan.ashell.core.data.remote.dto.GithubRepoStatsDto
import `in`.hridayan.ashell.core.domain.model.GithubRepoStats

fun mapToRepoStats(
    repoInfo: GithubRepoStatsDto,
    releases: List<GithubReleaseDto>
): GithubRepoStats {
    val totalDownloads = releases
        .flatMap { it.assets }
        .sumOf { it.downloadCount }

    val latestRelease = releases.firstOrNull()

    return GithubRepoStats(
        stars = repoInfo.stars,
        forks = repoInfo.forks,
        openIssues = repoInfo.openIssues,
        totalDownloadCount = totalDownloads,
        license = repoInfo.license?.spdxId
            ?.takeIf { it != "NOASSERTION" }
            ?: repoInfo.license?.name
            ?: "No license",
        latestVersion = latestRelease?.tagName ?: "N/A"
    )
}