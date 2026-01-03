package `in`.hridayan.ashell.core.data.local.mapper

import `in`.hridayan.ashell.core.data.local.model.GithubRepoStatsEntity
import `in`.hridayan.ashell.core.domain.model.GithubRepoStats


fun GithubRepoStatsEntity.toDomain(): GithubRepoStats {
    return GithubRepoStats(
        stars = stars,
        forks = forks,
        openIssues = issues,
        totalDownloadCount = downloads,
        license = license,
        latestVersion = latestVersion
    )
}

fun GithubRepoStats.toEntity(repo: String): GithubRepoStatsEntity {
    return GithubRepoStatsEntity(
        repo = repo,
        stars = stars,
        forks = forks,
        issues = openIssues,
        downloads = totalDownloadCount,
        license = license,
        latestVersion = latestVersion,
        lastUpdated = System.currentTimeMillis()
    )
}
