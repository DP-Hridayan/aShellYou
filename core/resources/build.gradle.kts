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
    api(libs.androidx.appcompat)
    api(libs.core.splashscreen)
    api(libs.material)
    testImplementation(libs.junit)
}