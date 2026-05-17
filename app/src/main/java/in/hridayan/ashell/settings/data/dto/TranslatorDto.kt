package `in`.hridayan.ashell.settings.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class TranslatorDto(
    val id: String,
    val username: String,
    val name: String,
    val translated: Int,
    val approved: Int,
    val picture: String,
    val languages: List<LanguageDto>,
    val avatar: String
)