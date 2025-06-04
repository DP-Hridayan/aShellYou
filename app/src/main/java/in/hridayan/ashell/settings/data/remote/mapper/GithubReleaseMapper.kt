package `in`.hridayan.ashell.settings.data.remote.mapper

import `in`.hridayan.ashell.settings.data.remote.dto.GitHubReleaseDto
import `in`.hridayan.ashell.settings.domain.model.GitHubRelease

fun GitHubReleaseDto.toDomain(): GitHubRelease {
    val apkAsset = assets.firstOrNull { it.name.endsWith(".apk") }
    return GitHubRelease(
        tagName = tagName,
        apkUrl = apkAsset?.browserDownloadUrl
    )
}
