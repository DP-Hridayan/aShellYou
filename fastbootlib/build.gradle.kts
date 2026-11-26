plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "in.hridayan.fastboot"

    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 28
    }

    testOptions {
        targetSdk = 37
    }

    lint {
        targetSdk = 37
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
    implementation(libs.core.ktx)
    testImplementation(libs.junit)
}
