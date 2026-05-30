package `in`.hridayan.ashell.settings.presentation.provider

import `in`.hridayan.settingsdsl.model.CustomSlot

/** Named custom-slot identifiers for the Backup & Restore screen. */
sealed class BackupScreenCustomSlots(id: String) : CustomSlot(id) {
    object GoogleSignIn : BackupScreenCustomSlots("google_sign_in")
    object LastBackupTime : BackupScreenCustomSlots("last_backup_time")
}
