plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.dependencyAnalysis)
}

android {
    namespace = "in.hridayan.ashell.core.resources"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 28
    }
}

dependencies {
    api(libs.androidx.appcompat)
    api(libs.core.splashscreen)
    api(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}

val generatedResDir = layout.buildDirectory.dir("generated/res/privacy_policy").get().asFile

val syncLegalDocs = tasks.register<Copy>("syncLegalDocs") {
    description =
        "Syncs the root PRIVACY_POLICY.md and TERMS_OF_SERVICE.md to a generated raw resources folder"
    from(rootProject.file("PRIVACY_POLICY.md"))
    from(rootProject.file("TERMS_OF_SERVICE.md"))
    into(File(generatedResDir, "raw"))
    rename("PRIVACY_POLICY.md", "privacy_policy.md")
    rename("TERMS_OF_SERVICE.md", "terms_of_service.md")
}

android {
    sourceSets {
        getByName("main") {
            res.srcDir(generatedResDir)
        }
    }
}

tasks.configureEach {
    if (name == "preBuild") {
        dependsOn(syncLegalDocs)
    }
}

