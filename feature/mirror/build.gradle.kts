plugins {
    alias(libs.plugins.ashell.android.feature)
    alias(libs.plugins.scrcpy.server.assets)
}

android {
    namespace = "in.hridayan.ashell.mirror"

    defaultConfig {
        buildConfigField("String", "SCRCPY_SERVER_VERSION", "\"${libs.versions.scrcpyServer.get()}\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(libs.material.icons.extended)
}
