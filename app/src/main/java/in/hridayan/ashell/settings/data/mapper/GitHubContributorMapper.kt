package `in`.hridayan.ashell.settings.data.mapper

import `in`.hridayan.ashell.settings.data.dto.GitHubContributorDto
import `in`.hridayan.ashell.settings.domain.model.GitHubContributor

fun GitHubContributorDto.toGitHubContributor(): GitHubContributor {
    return GitHubContributor(
        name = name,
        username = username,
        profileUrl = profileUrl,
        avatarAssetPath = avatar,
        contributions = contributions
    )
}
