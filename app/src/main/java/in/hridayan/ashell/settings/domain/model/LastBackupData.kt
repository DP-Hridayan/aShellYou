package `in`.hridayan.ashell.settings.domain.model

data class LastBackupData(
    val localTime: String = "",
    val localType: String = "",
    val cloudTime: String = "",
    val cloudType: String = ""
)
