plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.dependencyAnalysis)
}

android {
    namespace = "in.hridayan.ashell.core.navigation"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

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