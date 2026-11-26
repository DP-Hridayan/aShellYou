plugins {
    alias(libs.plugins.ashell.android.feature)
    alias(libs.plugins.ashell.android.room)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "in.hridayan.ashell.portforward"
}

dependencies {
    implementation(project(":core:resources"))

    implementation(libs.lifecycle.process)
    implementation(libs.material.icons.extended)
    implementation(libs.navigation.compose)
    implementation(libs.compose.settings.graph)
    implementation(libs.serialization.json)
}
