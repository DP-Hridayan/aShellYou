import com.android.build.api.artifact.SingleArtifact

plugins {
    alias(libs.plugins.android.application)
}

private val scrcpyVersion = libs.versions.scrcpyServer.get()
private val upstreamServer = rootProject.layout.projectDirectory.dir("third_party/scrcpy/server/src/main")
private val serverJarName = "scrcpy-server-v$scrcpyVersion.jar"

android {
    namespace = "com.genymobile.scrcpy"

    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.genymobile.scrcpy"
        minSdk = 21
        targetSdk = 37
        versionCode = 40100
        versionName = scrcpyVersion
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile(upstreamServer.file("AndroidManifest.xml"))
            java.directories.add(upstreamServer.dir("java").asFile.path)
            aidl.directories.add(upstreamServer.dir("aidl").asFile.path)
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            @Suppress("UnstableApiUsage")
            vcsInfo.include = false
        }
    }

    buildFeatures {
        buildConfig = true
        aidl = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

// The server is pure Java, but AGP's built-in Kotlin adds kotlin-stdlib to every module, which grew
// the upstream 4.x release from about 90 KB to about 730 KB without adding behaviour.
configurations.configureEach {
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
}

@Suppress("UnstableApiUsage")
val scrcpyServerJar = configurations.consumable("scrcpyServerJar") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "scrcpy-server-jar"))
    }
}

androidComponents {
    onVariants(selector().withBuildType("release")) { variant ->
        val jarName = serverJarName
        val packageJar = tasks.register<Sync>("packageScrcpyServerJar") {
            from(variant.artifacts.get(SingleArtifact.APK)) {
                include("*.apk")
                rename { jarName }
            }
            into(layout.buildDirectory.dir("scrcpy"))
        }

        artifacts.add(scrcpyServerJar.name, layout.buildDirectory.dir("scrcpy")) {
            builtBy(packageJar)
        }
    }
}
