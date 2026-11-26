plugins {
    alias(libs.plugins.ashell.android.feature)
}

android {
    namespace = "in.hridayan.ashell.adbsideload"

    defaultConfig {
        consumerProguardFiles("consumer-rules.keep")
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":adblib"))

    implementation(libs.androidx.compose.animation)
    implementation(libs.material.icons.extended)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
