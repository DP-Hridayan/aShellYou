package `in`.hridayan.ashell.shell.fastboot.domain.model

import com.google.android.fastbootmobile.ResponseStatus

data class FastbootCommandResult(
    val command: String,
    val status: ResponseStatus,
    val data: String,
    val timestamp: Long = System.currentTimeMillis()
)
