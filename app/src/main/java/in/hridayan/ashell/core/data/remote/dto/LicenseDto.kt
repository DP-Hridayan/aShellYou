package `in`.hridayan.ashell.core.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LicenseDto(

    @SerialName("spdx_id")
    val spdxId: String?,

    @SerialName("name")
    val name: String,

    @SerialName("url")
    val url: String
)