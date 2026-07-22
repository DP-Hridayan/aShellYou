plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.cgutman.adblib"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 14
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
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(fileTree("libs") { include("*.jar") })
    testImplementation(libs.junit)
}
