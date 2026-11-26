plugins {
    alias(libs.plugins.ashell.android.feature)
}

android {
    namespace = "in.hridayan.ashell.guides"

    defaultConfig {
        consumerProguardFiles("consumer-rules.keep")
    }
}

dependencies {
    implementation(project(":core:resources"))
    implementation(libs.androidx.compose.animation)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
