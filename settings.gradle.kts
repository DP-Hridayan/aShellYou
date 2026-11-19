pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }

    versionCatalogs {
        create("libs")
    }
}

rootProject.name = "aShell You"

includeBuild("build-logic")

val scrcpyManifest = file("third_party/scrcpy/server/src/main/AndroidManifest.xml")
if (!scrcpyManifest.exists() && file(".git").exists()) {
    logger.lifecycle("Submodule 'third_party/scrcpy' missing. Initializing git submodules...")
    try {
        ProcessBuilder("git", "submodule", "update", "--init", "--recursive")
            .directory(rootDir)
            .inheritIO()
            .start()
            .waitFor()
    } catch (e: Exception) {
        logger.warn("Could not auto-initialize git submodules: ${e.message}")
    }
}

include(":adblib")
include(":app")
include(":fastbootlib")
include(":libadb")
include(":scrcpyserver")

include(":core:common")
include(":core:navigation")
include(":core:resources")
include(":core:shizuku")
include(":core:ui")

include(":feature:ai")
include(":feature:commandexamples")
include(":feature:crashreporter")
include(":feature:home")
include(":feature:logcat")
include(":feature:onboarding")
include(":feature:qstiles")
include(":feature:settings")
include(":feature:shell")
include(":feature:adbsideload")
include(":feature:mirror")
include(":feature:portforward")
include(":feature:guides")
