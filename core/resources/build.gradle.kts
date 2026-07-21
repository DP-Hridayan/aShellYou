plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "in.hridayan.ashell.core.resources"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 28
    }
}

dependencies {
    testImplementation(libs.junit)
}