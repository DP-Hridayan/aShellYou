import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun VersionCatalog.pluginId(alias: String): String =
    findPlugin(alias).get().get().pluginId

internal fun VersionCatalog.library(alias: String): Provider<MinimalExternalModuleDependency> =
    findLibrary(alias).get()
