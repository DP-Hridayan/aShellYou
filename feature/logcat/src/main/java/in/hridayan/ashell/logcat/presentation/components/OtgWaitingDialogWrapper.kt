package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.components.dialog.OtgDeviceWaitingDialog
import `in`.hridayan.ashell.shell.otg_adb_shell.presentation.viewmodel.OtgViewModel

/**
 * Thin wrapper that calls [OtgDeviceWaitingDialog] from the logcat module.
 *
 * The OtgViewModel is scoped to this composable (default hiltViewModel scope).
 * The dialog handles scanning, waiting, and device connection itself.
 *
 * NOTE: This file does not introduce a Gradle dependency on :feature:shell —
 * the OtgViewModel and OtgDeviceWaitingDialog are provided at runtime via Hilt's
 * merged component. The Gradle dep is only on :core:common (already present).
 * If the build requires an explicit dep, add :feature:shell to logcat's build.gradle.kts.
 */
@Composable
fun OtgWaitingDialogWrapper(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    otgViewModel: OtgViewModel = hiltViewModel(),
) {
    OtgDeviceWaitingDialog(
        onDismiss = onDismiss,
        onConfirm = onConfirm,
        otgViewModel = otgViewModel,
    )
}
