package `in`.hridayan.ashell.logcat.domain.repository

import `in`.hridayan.ashell.logcat.domain.model.InstalledApp

interface InstalledAppsRepository {
    /** Every app installed on this device, system apps included, sorted by display name. */
    suspend fun installedApps(): Result<List<InstalledApp>>
}
