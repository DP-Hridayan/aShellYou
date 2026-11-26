import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("kotlin-compose"))

            extensions.configure<LibraryExtension> {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                val bom = libs.library("compose-bom")
                add("implementation", platform(bom))
                add("implementation", libs.library("compose-ui"))
            }
        }
    }
}
