buildscript {
    dependencies {
        classpath(libs.kotlin.gradle)
        classpath(libs.kotlin.metadata.jvm)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false

    alias(libs.plugins.dependencyAnalysis)
}

dependencyAnalysis {
    reporting {
        printBuildHealth(true)
    }
}

tasks.register<Delete>("clean") {
    description = "Deletes the root build directory."
    delete(rootProject.layout.buildDirectory)
}
