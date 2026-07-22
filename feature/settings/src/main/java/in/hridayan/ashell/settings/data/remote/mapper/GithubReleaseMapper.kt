package `in`.hridayan.ashell.settings.data.remote.mapper

import `in`.hridayan.ashell.core.common.domain.model.GithubReleaseType
import `in`.hridayan.ashell.settings.data.remote.dto.GithubReleaseDto
import `in`.hridayan.ashell.settings.domain.model.GithubRelease

fun GithubReleaseDto.toDomain(releaseType: Int): GithubRelease {
    val flavor = if (releaseType == GithubReleaseType.STABLE_FDROID) {
        "fdroid"
    } else {
        "github"
    }
    val apkAsset = assets.firstOrNull {
        it.name.contains(flavor, ignoreCase = true) && it.name.endsWith(".apk")
    }

    return GithubRelease(
        tagName = tagName,
        apkUrl = apkAsset?.browserDownloadUrl,
        body = body
    )
}
