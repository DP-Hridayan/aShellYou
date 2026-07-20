# Fix "Failed to resolve: androidx.compose.ui:ui-test-junit4" Error

The user is experiencing a Gradle sync error where the `androidx.compose.ui:ui-test-junit4` dependency cannot be resolved. This is likely because the `androidTestImplementation` configuration does not have the Compose BOM applied, or the BOM version `2026.06.01` does not include this specific artifact without an explicit version.

## Proposed Changes

### [gradle](file:///C:/Users/hrida/StudioProjects/aShellYou/gradle/libs.versions.toml)

#### [MODIFY] [libs.versions.toml](file:///C:/Users/hrida/StudioProjects/aShellYou/gradle/libs.versions.toml)

- Add a version reference for Compose UI components if not already present, or use the existing `composeAnimation` version (1.11.4) for `ui-test-junit4` to ensure it resolves.
- Alternatively, ensure `compose-ui-test-junit4` and `compose-ui-test-manifest` use an explicit version since other Compose libraries in this project (like `animation` and `material3`) do.

### [app](file:///C:/Users/hrida/StudioProjects/aShellYou/app/build.gradle.kts)

#### [MODIFY] [build.gradle.kts](file:///C:/Users/hrida/StudioProjects/aShellYou/app/build.gradle.kts)

- Add `androidTestImplementation(platform(libs.compose.bom))` to ensure the BOM is applied to the instrumentation test configuration. This is standard practice for Compose projects.

## Verification Plan

### Automated Tests
- Run Gradle sync to verify the issue is resolved.
- Run `:app:assembleDebugAndroidTest` to ensure test dependencies are correctly resolved.

### Manual Verification
- Verify that the error message in Android Studio disappears after sync.
