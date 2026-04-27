import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.release.lint.baseline)
}

android {
    namespace = "in.hridayan.ashell"

    compileSdk {
        version = release(36)
    }

    val flavorGitHub = "github"
    val flavorFDroid = "fdroid"

    defaultConfig {
        applicationId = "in.hridayan.ashell"
        minSdk = 28
        targetSdk = 36
        versionCode = 59
        versionName = "v7.2.0"

        buildConfigField("String", "DIST_FLAVOR_GITHUB", "\"$flavorGitHub\"")
        buildConfigField("String", "DIST_FLAVOR_FDROID", "\"$flavorFDroid\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        baseline = file("lint-baseline.xml")
    }

    signingConfigs {
        create("release") {
            val keystoreProperties = Properties()
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            val isCI = System.getenv("CI")?.toBoolean() == true

            when {
                keystorePropertiesFile.exists() -> {
                    keystorePropertiesFile.inputStream().use {
                        keystoreProperties.load(it)
                    }

                    storeFile = file(keystoreProperties["storeFile"] as String)
                    storePassword = keystoreProperties["storePassword"] as String
                    keyAlias = keystoreProperties["keyAlias"] as String
                    keyPassword = keystoreProperties["keyPassword"] as String
                }

                isCI -> {
                    val path = System.getenv("KEYSTORE_PATH")
                    val password = System.getenv("KEYSTORE_PASSWORD")
                    val alias = System.getenv("KEY_ALIAS")
                    val keyPass = System.getenv("KEY_PASSWORD") ?: password

                    if (!path.isNullOrBlank() && !password.isNullOrBlank() && !alias.isNullOrBlank()) {
                        storeFile = file(path)
                        storePassword = password
                        keyAlias = alias
                        keyPassword = keyPass
                    }
                }
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.findByName("release")
        }
    }

    flavorDimensions.add("distribution")

    productFlavors {
        create(flavorGitHub) {
            dimension = "distribution"
        }
        create(flavorFDroid) {
            dimension = "distribution"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/*",
                "/META-INF/versions/**"
            )
        }

        jniLibs {
            useLegacyPackaging = false
        }
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
}

androidComponents {
    onVariants { variant ->
        if (variant.buildType == "release") {
            variant.outputs.forEach { output ->

                val flavor = variant.flavorName ?: "noflavor"
                val version = android.defaultConfig.versionName ?: "unknown"

                @Suppress("UnstableApiUsage")
                output.outputFileName.set(
                    "aShellYou-${version}-${flavor}-release.apk"
                )
            }
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

tasks.withType<Zip>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

dependencies {
    implementation(project(":libadb"))
    implementation(project(":adblib"))

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.material)
    implementation(libs.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.datastore.preferences)
    implementation(libs.navigation.compose)

    implementation(libs.room.ktx)
    implementation(libs.androidx.documentfile)
    ksp(libs.room.compiler)

    implementation(libs.serialization.json)
    implementation(libs.gson)

    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.core.splashscreen)
    implementation(libs.androidx.compose.animation)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.work)

    implementation(libs.jmdns)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.slf4j.android)

    implementation(libs.androidx.security.crypto)

    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)
    implementation(libs.libsu.core)

    implementation(libs.lottie.compose)
    implementation(libs.nayuki.qrcode)
    implementation(libs.sun.security.android)
    implementation(libs.lsposed.hiddenapibypass)

    implementation(libs.shapeindicators)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    githubImplementations(
        libs.play.services.auth,
        libs.credentials,
        libs.credentials.play.services.auth,
        libs.google.id,
        libs.google.api.client.android,
        libs.google.api.services.drive,
        libs.google.http.client.gson
    )

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}