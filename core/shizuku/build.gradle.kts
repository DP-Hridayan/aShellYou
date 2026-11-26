plugins {
    alias(libs.plugins.ashell.android.library)
    alias(libs.plugins.ashell.android.hilt)
}

android {
    namespace = "in.hridayan.ashell.core.shizuku"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        aidl = true
    }
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.annotation)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
