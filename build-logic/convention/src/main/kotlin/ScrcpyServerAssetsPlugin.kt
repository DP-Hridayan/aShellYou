import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

private const val SERVER_PROJECT_PATH = ":scrcpyserver"
private const val SERVER_JAR_USAGE = "scrcpy-server-jar"
private const val DECLARED_CONFIGURATION = "scrcpyServer"
private const val RESOLVABLE_CONFIGURATION = "scrcpyServerFiles"
private const val GENERATED_ASSETS_DIR = "generated/scrcpyServerAssets"

/**
 * Packs the scrcpy server built by `:scrcpyserver` into this module's assets.
 *
 * The jar is matched by a dedicated usage attribute rather than a configuration name, so the
 * dependency resolves to the packaged server and never to the server module's Android outputs.
 */
class ScrcpyServerAssetsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val serverJarUsage = project.objects.named(Usage::class.java, SERVER_JAR_USAGE)
        val declared = project.configurations.dependencyScope(DECLARED_CONFIGURATION)
        val resolvable = project.configurations.resolvable(RESOLVABLE_CONFIGURATION) {
            extendsFrom(declared.get())
            attributes.attribute(Usage.USAGE_ATTRIBUTE, serverJarUsage)
        }

        project.dependencies.add(
            DECLARED_CONFIGURATION,
            project.dependencies.project(mapOf("path" to SERVER_PROJECT_PATH))
        )

        val syncAssets = project.tasks.register(
            "syncScrcpyServerAssets",
            SyncScrcpyServerAssetsTask::class.java
        ) {
            serverFiles.from(resolvable)
            outputDirectory.set(project.layout.buildDirectory.dir(GENERATED_ASSETS_DIR))
        }

        project.plugins.withId("com.android.library") {
            project.extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
                variant.sources.assets?.addGeneratedSourceDirectory(
                    syncAssets,
                    SyncScrcpyServerAssetsTask::outputDirectory
                )
            }
        }
    }
}

abstract class SyncScrcpyServerAssetsTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NAME_ONLY)
    abstract val serverFiles: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Inject
    abstract val fileSystem: FileSystemOperations

    @TaskAction
    fun sync() {
        fileSystem.sync {
            from(serverFiles)
            into(outputDirectory)
        }
    }
}
