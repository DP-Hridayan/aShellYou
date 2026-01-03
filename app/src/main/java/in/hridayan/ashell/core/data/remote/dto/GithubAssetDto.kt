package `in`.hridayan.ashell.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubAssetDto(
    @SerialName("name")
    val name: String,

    @SerialName("browser_download_url")
    val browserDownloadUrl: String,

    @SerialName("download_count")
    val downloadCount: Long
)