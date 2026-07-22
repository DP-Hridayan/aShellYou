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

include(":adblib")
include(":app")
include(":fastbootlib")
include(":libadb")
include(":settings-dsl")

include(":core:common")
include(":core:navigation")
include(":core:resources")
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
