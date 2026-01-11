import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ReleaseLintBaselinePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("com.android.application") {

            val androidComponents =
                project.extensions.getByType(AndroidComponentsExtension::class.java)

            project.tasks.named("lintRelease") {
                outputs.upToDateWhen { false }
            }

            val updateBaseline =
                project.tasks.register("updateLintReleaseBaseline") {
                    group = "lint"
                    description = "Update lint baseline for release builds"
                    dependsOn("lintRelease")
                }

            androidComponents.onVariants { variant ->
                if (variant.buildType == "release") {
                    project.tasks.named("assembleRelease") {
                        dependsOn(updateBaseline)
                    }
                    project.tasks.named("bundleRelease") {
                        dependsOn(updateBaseline)
                    }
                }
            }
        }
    }
}
