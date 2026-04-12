package `in`.hridayan.ashell.settings.domain.event

import android.content.IntentSender

sealed class DriveAuthEvent {
    data class ConsentRequired(val intentSender: IntentSender) : DriveAuthEvent()
}