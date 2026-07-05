package `in`.hridayan.ashell.settings.domain.model

data class LastBackupData(
    val localTime: String = "",
    val localType: String = "",
    val localIsAuto: Boolean = false,
    val cloudTime: String = "",
    val cloudType: String = "",
    val cloudIsAuto: Boolean = false,
)
