package `in`.hridayan.ashell.settings.domain.model

import `in`.hridayan.ashell.core.data.model.AttendanceEntity
import `in`.hridayan.ashell.core.data.model.SubjectEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val settings: Map<String, String?>? = null,
    val attendance: List<AttendanceEntity>? = null,
    val subjects: List<SubjectEntity>? = null,
    val backupTime: String
)
