package `in`.hridayan.ashell.settings.data.parser

import android.content.Context
import `in`.hridayan.ashell.settings.data.dto.GitHubContributorDto
import kotlinx.serialization.json.Json

object GitHubContributorParser {

    fun loadJson(context: Context): List<GitHubContributorDto> {

        val json = context.assets
            .open("github/contributors.json")
            .bufferedReader()
            .use { it.readText() }

        return Json.decodeFromString(json)
    }
}
