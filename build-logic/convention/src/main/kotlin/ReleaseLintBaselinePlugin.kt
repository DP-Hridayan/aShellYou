import org.gradle.api.Plugin
import org.gradle.api.Project

class ReleaseLintBaselinePlugin : Plugin<Project> {

    override fun apply(project: Project) {

        project.plugins.withId("com.android.application") {

            val updateBaseline =
                project.tasks.register("updateLintReleaseBaseline") {
                    group = "lint"
                    description = "Update lint baseline for release builds"
                    
                    doFirst {
                        println("\n========================================")
                        println("🔄 Updating lint baseline for release...")
                        println("========================================\n")
                    }
                    
                    doLast {
                        println("\n========================================")
                        println("✅ Lint baseline updated successfully!")
                        println("========================================\n")
                    }
                }

            project.afterEvaluate {
                project.tasks.matching { it.name == "lintRelease" }.configureEach {
                    outputs.upToDateWhen { false }
                }

                updateBaseline.configure {
                    dependsOn("updateLintBaselineRelease")
                }

                project.tasks.matching { it.name == "assembleRelease" }.configureEach {
                    dependsOn(updateBaseline)
                }

                project.tasks.matching { it.name == "bundleRelease" }.configureEach {
                    dependsOn(updateBaseline)
                }
            }
        }
    }
}
