package `in`.hridayan.ashell.core.domain.model

data class GithubRelease(
    val tagName: String,
    val apkUrl: String? = null
)
