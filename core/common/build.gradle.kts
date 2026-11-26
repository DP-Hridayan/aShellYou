plugins {
    alias(libs.plugins.ashell.android.library)
    alias(libs.plugins.ashell.android.compose)
    alias(libs.plugins.ashell.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "in.hridayan.ashell.core.common"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(project(":core:resources"))
    api(project(":adblib"))

    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.material)

    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    api(libs.datastore.preferences)
    api(libs.serialization.json)
    implementation(libs.androidx.security.crypto)

    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.libsu.core)
    implementation(libs.jmdns)

    implementation(libs.androidx.documentfile)
    implementation(libs.compose.settings.graph)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
