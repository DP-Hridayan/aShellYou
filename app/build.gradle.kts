plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "in.hridayan.ashell"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        targetSdk = 35
        versionCode = 52
        versionName = "v6.0.2"

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    signingConfigs {
        create("release") {
            if (System.getenv("CI") == "true") {
                storeFile = file(System.getenv("KEYSTORE_PATH"))
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    applicationVariants.configureEach {
        outputs.forEach { output ->
            if (output is com.android.build.gradle.api.ApkVariantOutput) {
                output.outputFileName = "aShell You ${versionName}-${buildType.name}.apk"
            }
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    androidResources {
        generateLocaleConfig = true
    }

    packagingOptions {
        jniLibs.useLegacyPackaging = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val versions = mapOf(
        "lifecycle" to "2.7.0",
        "material" to "1.13.0-alpha11",
        "preference" to "1.2.1",
        "shizuku" to "13.1.0",
        "libsu" to "6.0.0",
        "glide" to "4.16.0",
        "lottie" to "6.6.4"
    )

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("com.google.code.gson:gson:2.12.1")
    implementation("com.google.android.material:material:${versions["material"]}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${versions["lifecycle"]}")
    implementation("androidx.work:work-runtime:2.10.0")
    implementation("androidx.preference:preference-ktx:${versions["preference"]}")
    implementation("dev.rikka.shizuku:api:${versions["shizuku"]}")
    implementation("com.github.topjohnwu.libsu:core:${versions["libsu"]}")
    implementation("dev.rikka.shizuku:provider:${versions["shizuku"]}")
    implementation("com.github.bumptech.glide:glide:${versions["glide"]}")
    annotationProcessor("com.github.bumptech.glide:compiler:${versions["glide"]}")
    implementation("com.airbnb.android:lottie:${versions["lottie"]}")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation(project(":adblib"))
    implementation(project(":changelogs"))

    testImplementation("junit:junit:4.13.2")
}