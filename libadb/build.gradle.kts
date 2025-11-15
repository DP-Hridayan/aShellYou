// SPDX-License-Identifier: GPL-3.0-or-later OR Apache-2.0

plugins {
    alias(libs.plugins.android.library)
}

group = "io.github.muntashirakon"
version = "3.1.0"

android {
    namespace = "io.github.muntashirakon.adb"
    compileSdk = 36

    defaultConfig {
        minSdk = 14
        aarMetadata {
            minCompileSdk = 1
        }
    }

    testOptions {
        targetSdk = 36
    }

    lint {
        baseline = file("lint-baseline.xml")
        targetSdk = 36
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.annotation)
    implementation(libs.bcprov.jdk15to18)
    implementation(libs.spake2.android)

    testImplementation(libs.junit)
}