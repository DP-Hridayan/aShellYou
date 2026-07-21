package `in`.hridayan.ashell.settings.data.repository

import android.content.Context
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withJson
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.settings.domain.model.LibraryItem
import `in`.hridayan.ashell.settings.domain.repository.LicensesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data-layer implementation of [LicensesRepository].
 *
 * Uses AboutLibraries **only** as a metadata provider — no UI from the library is used.
 *
 * The Gradle plugin (`com.mikepenz.aboutlibraries.plugin`) auto-generates
 * `R.raw.aboutlibraries` during every build — no manual export step is needed.
 * We read the raw resource directly and feed its JSON string to [Libs.Builder.withJson],
 * which is explicit and immune to any resource-discovery quirks across build variants.
 */
class LicensesRepositoryImpl(
    private val context: Context,
) : LicensesRepository {

    override suspend fun getLibraries(): List<LibraryItem> = withContext(Dispatchers.IO) {
        val json = runCatching {
            context.resources.openRawResource(R.raw.aboutlibraries)
                .bufferedReader()
                .use { it.readText() }
        }.getOrNull() ?: return@withContext emptyList()

        val libs = Libs.Builder().withJson(json).build()

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
