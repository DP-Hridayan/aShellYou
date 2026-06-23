package `in`.hridayan.ashell.settings.domain.model

/**
 * Predefined backup frequency options for automatic scheduled backups.
 * Int values are stored via [SettingsKeys.AutoBackupFrequency].
 * String labels are mapped for WorkManager interval calculation.
 */
object BackupFrequency {
    const val DAILY = 0
    const val WEEKLY = 1
    const val MONTHLY = 2

    /** Convert int value to a human-readable label for WorkManager interval calculation. */
    fun intervalDays(value: Int): Long = when (value) {
        WEEKLY -> 7L
        MONTHLY -> 30L
        else -> 1L
    }
}
