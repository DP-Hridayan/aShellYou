package `in`.hridayan.ashell.settings.domain.repository

import `in`.hridayan.ashell.settings.domain.model.UpdateResult

interface UpdateRepository {
    suspend fun fetchLatestRelease(includePrerelease:Boolean): UpdateResult
}