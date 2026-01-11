plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.agp.gradle)
    compileOnly(libs.kotlin.gradle)
}

gradlePlugin {
    plugins {
        register("releaseLintBaseline") {
            id = "release.lint.baseline"
            implementationClass =
                "ReleaseLintBaselinePlugin"
        }
    }
}