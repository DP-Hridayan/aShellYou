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

            project.afterEvaluate {

                val baselineTasks = project.tasks.matching {
                    it.name.startsWith("updateLintBaseline") && it.name.endsWith("Release")
                }

                updateBaseline.configure {
                    dependsOn(baselineTasks)
                }

                project.tasks.matching {
                    it.name.startsWith("lint") && it.name.endsWith("Release")
                }.configureEach {
                    outputs.upToDateWhen { false }
                }

                project.tasks.matching {
                    it.name.startsWith("assemble") && it.name.endsWith("Release")
                }.configureEach {
                    dependsOn(updateBaseline)
                }

                project.tasks.matching {
                    it.name.startsWith("bundle") && it.name.endsWith("Release")
                }.configureEach {
                    dependsOn(updateBaseline)
                }
            }
        }
    }
}
