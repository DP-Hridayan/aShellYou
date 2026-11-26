plugins {
    alias(libs.plugins.ashell.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "in.hridayan.ashell.core.navigation"
}

dependencies {
    implementation(project(":core:common"))

    implementation(libs.serialization.json)
    implementation(libs.androidx.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.material)
    implementation(libs.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
