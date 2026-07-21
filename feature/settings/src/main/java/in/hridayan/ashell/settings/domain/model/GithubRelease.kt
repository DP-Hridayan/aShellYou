package `in`.hridayan.ashell.settings.domain.model

data class GithubRelease(
    val tagName: String,
    val apkUrl: String? = null,
    val body: String? = null
)
