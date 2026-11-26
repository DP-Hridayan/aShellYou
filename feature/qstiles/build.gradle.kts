plugins {
    alias(libs.plugins.ashell.android.feature)
    alias(libs.plugins.ashell.android.room)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

android {
    namespace = "in.hridayan.ashell.qstiles"
}

dependencies {
    implementation(project(":core:shizuku"))

    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.material.icons.extended)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.documentfile)
    implementation(libs.serialization.json)
    implementation(libs.gson)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.compose.animation)
    implementation(libs.hilt.work)
    implementation(libs.jmdns)
    implementation(libs.slf4j.android)
    implementation(libs.androidx.security.crypto)
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.libsu.core)
    implementation(libs.lottie.compose)
    implementation(libs.nayuki.qrcode)
    implementation(libs.sun.security.android)
    implementation(libs.shapeindicators)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.aboutlibraries.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
