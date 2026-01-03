package `in`.hridayan.ashell.core.data.remote.mapper

import `in`.hridayan.ashell.core.data.remote.dto.GithubReleaseDto
import `in`.hridayan.ashell.core.domain.model.GithubRelease

fun GithubReleaseDto.toDomain(): GithubRelease {
    val apkAsset = assets.firstOrNull { it.name.endsWith(".apk") }
    return GithubRelease(
        tagName = tagName,
        apkUrl = apkAsset?.browserDownloadUrl
    )
}
