package `in`.hridayan.ashell.core.domain.model

data class GithubRepoStats(
    val stars: Int,
    val forks: Int,
    val openIssues: Int,
    val totalDownloadCount: Long,
    val license : String,
    val latestVersion : String,
)
