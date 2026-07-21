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

include(":app", ":adblib", ":libadb", ":settings-dsl", ":fastbootlib")
includeBuild("build-logic")
include(":core:common")
include(":core:ui")
include(":core:navigation")
include(":feature:shell")
include(":feature:commandexamples")
include(":feature:qstiles")
include(":feature:onboarding")
include(":feature:home")
include(":feature:settings")
include(":feature:logcat")
include(":feature:crashreporter")
include(":feature:ai")
include(":core:resources")
