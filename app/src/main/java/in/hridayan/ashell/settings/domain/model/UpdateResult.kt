package `in`.hridayan.ashell.settings.domain.model

import `in`.hridayan.ashell.core.domain.model.GithubRelease

sealed class UpdateResult {
    data class Success(val release: GithubRelease, val isUpdateAvailable: Boolean) : UpdateResult()
    object NetworkError : UpdateResult()
    object Timeout : UpdateResult()
    object UnknownError : UpdateResult()
}
