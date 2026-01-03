package `in`.hridayan.ashell.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubReleaseDto(
    @SerialName("tag_name")
    val tagName: String,

    @SerialName("assets")
    val assets: List<GithubAssetDto> = emptyList()
)