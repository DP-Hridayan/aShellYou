package `in`.hridayan.ashell.settings.domain.repository

import `in`.hridayan.ashell.settings.domain.model.LibraryItem

/**
 * Contract for accessing open-source library metadata.
 * Implementations are responsible for transforming raw AboutLibraries data
 * into clean domain [LibraryItem] objects.
 */
interface LicensesRepository {

    /**
     * Returns all detected third-party libraries, sorted alphabetically by name.
     * This is a suspend function because reading/parsing JSON from assets
     * should happen off the main thread.
     */
    suspend fun getLibraries(): List<LibraryItem>
}
