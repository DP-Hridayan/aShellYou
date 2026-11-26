plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.agp.gradle)
    compileOnly(libs.kotlin.gradle)
    compileOnly(libs.ksp.gradle)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "ashell.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }

        register("androidCompose") {
            id = "ashell.android.compose"
            implementationClass = "AndroidComposeConventionPlugin"
        }

        register("androidHilt") {
            id = "ashell.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }

        register("androidRoom") {
            id = "ashell.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }

        register("androidFeature") {
            id = "ashell.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }

        register("releaseLintBaseline") {
            id = "release.lint.baseline"
            implementationClass = "ReleaseLintBaselinePlugin"
        }

        register("scrcpyServerAssets") {
            id = "scrcpy.server.assets"
            implementationClass = "ScrcpyServerAssetsPlugin"
        }
    }
}
