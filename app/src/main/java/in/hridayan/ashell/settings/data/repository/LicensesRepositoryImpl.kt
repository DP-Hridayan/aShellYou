package `in`.hridayan.ashell.settings.data.repository

import android.content.Context
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import `in`.hridayan.ashell.settings.domain.model.LibraryItem
import `in`.hridayan.ashell.settings.domain.repository.LicensesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data-layer implementation of [LicensesRepository].
 *
 * Uses AboutLibraries as a pure metadata provider:
 * - [Libs.Builder().withContext()] reads the pre-generated `aboutlibraries.json`
 *   that the Gradle plugin writes into the app's assets at build time.
 * - We then map the raw AboutLibraries model to our own clean [LibraryItem].
 *
 * No UI from AboutLibraries is used anywhere in this class.
 */
class LicensesRepositoryImpl(
    private val context: Context,
) : LicensesRepository {

    override suspend fun getLibraries(): List<LibraryItem> = withContext(Dispatchers.IO) {
        val libs = Libs.Builder().withContext(context).build()

        libs.libraries
            .map { library ->
                val license = library.licenses.firstOrNull()
                LibraryItem(
                    uniqueId = library.uniqueId,
                    name = library.name,
                    artifactId = library.artifactId,
                    version = library.artifactVersion,
                    developers = library.developers.mapNotNull { it.name },
                    website = library.website,
                    scmUrl = library.scm?.url,
                    licenseName = license?.name,
                    licenseUrl = license?.url,
                    licenseContent = license?.licenseContent,
                    description = library.description,
                )
            }
            .sortedBy { it.name.lowercase() }
    }
}
