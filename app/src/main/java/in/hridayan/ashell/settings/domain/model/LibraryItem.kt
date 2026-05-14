package `in`.hridayan.ashell.settings.domain.model

/**
 * Immutable domain model representing a single open-source dependency.
 *
 * Fields are nullable because not every library provides all metadata fields
 * (e.g., some libraries have no website or SCM URL in their POM).
 */
data class LibraryItem(
    val uniqueId: String,
    val name: String,
    val artifactId: String,
    val version: String?,
    val developers: List<String>,
    val website: String?,
    val scmUrl: String?,
    val licenseName: String?,
    val licenseUrl: String?,
    val licenseContent: String?,
    val description: String?,
)
