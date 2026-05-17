package `in`.hridayan.ashell.settings.data.mapper

import `in`.hridayan.ashell.settings.data.dto.TranslatorDto
import `in`.hridayan.ashell.settings.domain.model.Translator

fun TranslatorDto.toTranslator(): Translator {
    return Translator(
        name = name.replace(Regex("""\s*\(.*?\)"""), ""),
        languages = languages.map { it.name },
        avatarAssetPath = avatar
    )
}