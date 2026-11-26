plugins {
    alias(libs.plugins.ashell.android.feature)
    alias(libs.plugins.ashell.android.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.stability.analyzer)
    id("kotlin-parcelize")
}

android {
    namespace = "in.hridayan.ashell.ai"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
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
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.slf4j.android)
    implementation(libs.androidx.security.crypto)
    implementation(libs.compose.settings.graph)
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
    implementation(libs.tink.android)
    implementation(libs.markdown.renderer)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.highlights)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
