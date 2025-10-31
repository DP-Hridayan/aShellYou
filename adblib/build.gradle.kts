plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.cgutman.adblib"
    compileSdk = 36

    defaultConfig {
        minSdk = 14
        targetSdk = 36
    }

    lint {
        baseline = file("lint-baseline.xml")
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
    testImplementation("junit:junit:4.13.2")
}
