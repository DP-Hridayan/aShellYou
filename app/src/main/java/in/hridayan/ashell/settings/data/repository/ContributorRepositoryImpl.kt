package `in`.hridayan.ashell.settings.data.repository

import android.content.Context
import `in`.hridayan.ashell.settings.data.mapper.toGitHubContributor
import `in`.hridayan.ashell.settings.data.mapper.toTranslator
import `in`.hridayan.ashell.settings.data.parser.GitHubContributorParser
import `in`.hridayan.ashell.settings.data.parser.TranslatorParser
import `in`.hridayan.ashell.settings.domain.model.GitHubContributor
import `in`.hridayan.ashell.settings.domain.model.Translator
import `in`.hridayan.ashell.settings.domain.repository.ContributorsRepository

class ContributorsRepositoryImpl(
    private val context: Context
) : ContributorsRepository {

    override fun getTranslators(): List<Translator> {

        return TranslatorParser
            .loadJson(context)
            .sortedByDescending { it.translated }
            .map { it.toTranslator() }
    }

    override fun getGitHubContributors(): List<GitHubContributor> {

        return GitHubContributorParser
            .loadJson(context)
            .sortedByDescending { it.contributions }
            .map { it.toGitHubContributor() }
    }
}