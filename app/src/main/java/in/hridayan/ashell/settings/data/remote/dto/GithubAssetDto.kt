package `in`.hridayan.ashell.settings.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GitHubAssetDto(
    @SerialName("name")
    val name: String,

    @SerialName("browser_download_url")
    val browserDownloadUrl: String
)