plugins {
    alias(libs.plugins.android.application)
}

android {
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    namespace = "in.hridayan.changelogs"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}