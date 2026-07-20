# Walkthrough - Fixed "ui-test-junit4" Resolution Issue

I have fixed the issue where the `androidx.compose.ui:ui-test-junit4` dependency could not be resolved during Gradle sync, and also resolved the subsequent "declared multiple times" error.

## Changes Made

### [gradle](file:///C:/Users/hrida/StudioProjects/aShellYou/gradle/libs.versions.toml)

#### [libs.versions.toml](file:///C:/Users/hrida/StudioProjects/aShellYou/gradle/libs.versions.toml)

- Added `composeUiTest = "1.11.4"` to the `[versions]` block.
- Updated `compose-ui-test-junit4` and `compose-ui-test-manifest` to use this explicit version.
- This ensures these dependencies resolve correctly without relying on the Compose BOM, which was failing to provide versions for them in the instrumentation test configuration.

### [app](file:///C:/Users/hrida/StudioProjects/aShellYou/app/build.gradle.kts)

#### [build.gradle.kts](file:///C:/Users/hrida/StudioProjects/aShellYou/app/build.gradle.kts)

- Removed the duplicate `platform(libs.compose.bom)` from `androidTestImplementation` to resolve the "declared multiple times" error.
- The dependencies now resolve using the explicit versions defined in the version catalog.

## Verification Results

### Gradle Sync
- Successfully executed Gradle sync. Both the "Failed to resolve" and "Declared multiple times" errors are resolved.
