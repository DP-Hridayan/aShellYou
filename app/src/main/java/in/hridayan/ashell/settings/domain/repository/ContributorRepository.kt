package `in`.hridayan.ashell.settings.domain.repository

import `in`.hridayan.ashell.settings.domain.model.GitHubContributor
import `in`.hridayan.ashell.settings.domain.model.Translator

interface ContributorsRepository {

    fun getTranslators(): List<Translator>

    fun getGitHubContributors(): List<GitHubContributor>
}