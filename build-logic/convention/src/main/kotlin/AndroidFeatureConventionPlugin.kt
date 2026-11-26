import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("ashell.android.library")
            pluginManager.apply("ashell.android.compose")
            pluginManager.apply("ashell.android.hilt")

            dependencies {
                add("implementation", project(":core:common"))
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:navigation"))

                add("implementation", libs.library("compose-ui-graphics"))
                add("implementation", libs.library("compose-ui-tooling-preview"))
                add("implementation", libs.library("material3"))
                add("implementation", libs.library("lifecycle-runtime-compose"))
                add("implementation", libs.library("lifecycle-viewmodel-ktx"))
                add("implementation", libs.library("lifecycle-viewmodel-compose"))
                add("implementation", libs.library("activity-compose"))
                add("implementation", libs.library("hilt-navigation-compose"))
                add("implementation", libs.library("core-ktx"))

                add("testImplementation", libs.library("junit"))
                add("testImplementation", libs.library("kotlinx-coroutines-test"))
            }
        }
    }
}
