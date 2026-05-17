package `in`.hridayan.ashell.settings.data.parser

import android.content.Context
import `in`.hridayan.ashell.settings.data.dto.TranslatorDto
import kotlinx.serialization.json.Json

object TranslatorParser {

    fun loadJson(context: Context): List<TranslatorDto> {

        val json = context.assets
            .open("crowdin/translators.json")
            .bufferedReader()
            .use { it.readText() }

        return Json.decodeFromString(json)
    }
}