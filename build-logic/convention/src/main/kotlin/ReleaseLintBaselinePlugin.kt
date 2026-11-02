import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ReleaseLintBaselinePlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.plugins.withId("com.android.application") {

            val updateBaseline = project.tasks.register("updateLintReleaseBaseline") {
                group = "lint"
                description = "Update lint baseline for all release variants"

                doFirst {
                    println("\n========================================")
                    println("🔄 Updating lint baseline for release variants...")
                    println("========================================\n")
                }

                doLast {
                    println("\n========================================")
                    println("✅ Lint baseline updated successfully!")
                    println("========================================\n")
                }
            }

            val androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension::class.java)
            androidComponents.onVariants(androidComponents.selector().withBuildType("release")) { variant ->
                val variantNameCapitalized = variant.name.replaceFirstChar { it.uppercase() }
                val updateLintBaselineTaskName = "updateLintBaseline$variantNameCapitalized"
                val lintTaskName = "lint$variantNameCapitalized"
                val assembleTaskName = "assemble$variantNameCapitalized"
                val bundleTaskName = "bundle$variantNameCapitalized"

                updateBaseline.configure {
                    dependsOn(updateLintBaselineTaskName)
                }

                project.tasks.configureEach {
                    if (name == assembleTaskName || name == bundleTaskName) {
                        dependsOn(updateLintBaselineTaskName)
                    }
                    if (name == lintTaskName) {
                        outputs.upToDateWhen { false }
                    }
                }
            }
        }
    }
}
