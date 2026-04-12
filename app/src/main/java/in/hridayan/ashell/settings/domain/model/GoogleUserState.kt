package `in`.hridayan.ashell.settings.domain.model

import android.net.Uri

data class GoogleUserState(
    val isSignedIn: Boolean = false,
    val email: String? = null,
    val name: String? = null,
    val photoUrl: Uri? = null
)