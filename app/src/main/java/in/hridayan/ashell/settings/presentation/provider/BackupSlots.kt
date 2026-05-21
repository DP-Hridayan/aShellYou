package `in`.hridayan.ashell.settings.presentation.provider

import `in`.hridayan.settingsdsl.model.CustomSlot

/** Named custom-slot identifiers for the Backup & Restore screen. */
sealed class BackupSlots(id: String) : CustomSlot(id) {
    object GoogleSignIn : BackupSlots("google_sign_in")
    object LastBackupTime : BackupSlots("last_backup_time")
}
