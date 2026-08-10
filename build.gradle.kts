import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask

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
    alias(libs.plugins.detekt)
}

dependencyAnalysis {
    reporting {
        printBuildHealth(true)
    }
}

subprojects {
    pluginManager.apply(rootProject.libs.plugins.detekt.get().pluginId)

    detekt {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("$rootDir/detekt.yml"))
        baseline = file("$projectDir/detekt-baseline.xml")
    }

    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }
}

tasks.register("detektAll") {
    description = "Runs detekt on all subprojects."
    dependsOn(subprojects.map { it.tasks.withType<Detekt>() })
}

tasks.register("detektBaselineAll") {
    description = "Generates a Detekt baseline for all modules"
    dependsOn(subprojects.map { it.tasks.withType<DetektCreateBaselineTask>() })
}

tasks.register<Delete>("clean") {
    description = "Deletes the root build directory."
    delete(rootProject.layout.buildDirectory)
}
