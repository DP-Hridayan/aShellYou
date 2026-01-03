package `in`.hridayan.ashell.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRepoStatsDto(
    @SerialName("stargazers_count")
    val stars: Int,

    @SerialName("forks_count")
    val forks: Int,

    @SerialName("open_issues_count")
    val openIssues: Int,

    @SerialName("license")
    val license: LicenseDto?
)
