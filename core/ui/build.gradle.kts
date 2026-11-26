plugins {
    alias(libs.plugins.ashell.android.library)
    alias(libs.plugins.ashell.android.compose)
    alias(libs.plugins.ashell.android.hilt)
    alias(libs.plugins.ashell.android.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.stability.analyzer)
}

android {
    namespace = "in.hridayan.ashell.core.ui"
}

dependencies {
    implementation(project(":core:resources"))
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))

    api(libs.androidx.biometric)
    implementation(libs.androidx.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.lottie.compose)
    implementation(libs.coil.compose)
    implementation(libs.serialization.json)
    implementation(libs.compose.settings.graph)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
    debugImplementation(libs.compose.ui.tooling)
}
