import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

private const val COMPILE_SDK_MAJOR = 37
private const val COMPILE_SDK_MINOR = 1
private const val MIN_SDK_VERSION = 28
private const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"

class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply(libs.pluginId("android-library"))
            pluginManager.apply(libs.pluginId("dependencyAnalysis"))

            extensions.configure<LibraryExtension> {
                compileSdk {
                    version = release(COMPILE_SDK_MAJOR) {
                        minorApiLevel = COMPILE_SDK_MINOR
                    }
                }

                defaultConfig {
                    minSdk = MIN_SDK_VERSION
                    testInstrumentationRunner = TEST_INSTRUMENTATION_RUNNER
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }
            }
        }
    }
}
