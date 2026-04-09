package `in`.hridayan.ashell.settings.domain.repository

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

interface GoogleAuthRepository {
    val isSignedIn: StateFlow<Boolean>
    val userEmail: StateFlow<String?>
    val userName: StateFlow<String?>
    val userPhotoUrl: StateFlow<Uri?>

    /**
     * Launch sign-in via Credential Manager (modern bottom sheet) and request Drive scope.
     * Returns Result.success(email) or Result.failure(exception).
     */
    suspend fun signIn(context: Context): Result<String>

    /** Sign out and clear stored credential. */
    suspend fun signOut()

    /** Get account email for building Drive credential. Returns null if not signed in. */
    fun getAccountEmail(): String?
}
