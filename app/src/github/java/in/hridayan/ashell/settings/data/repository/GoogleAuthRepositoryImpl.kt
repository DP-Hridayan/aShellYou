package `in`.hridayan.ashell.settings.data.repository

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.hridayan.ashell.settings.data.SettingsKeys
import `in`.hridayan.ashell.settings.domain.model.GoogleUserState
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import `in`.hridayan.ashell.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class GoogleAuthRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : GoogleAuthRepository {

    companion object {
        private const val TAG = "GoogleAuth"
        private const val WEB_CLIENT_ID =
            "881968854575-28tp1junqrp9ta8qma5kl8ittgclmrji.apps.googleusercontent.com"
    }

    override val isAvailable: Boolean = true

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _googleUserState = MutableStateFlow(GoogleUserState())

    override val googleUserState: StateFlow<GoogleUserState> =
        _googleUserState.asStateFlow()

    init {
        restoreState()
    }

    private fun restoreState() {
        scope.launch {
            try {
                val savedEmail = settingsRepository
                    .getString(SettingsKeys.GOOGLE_ACCOUNT_EMAIL)
                    .first()

                val savedPhotoUrl = settingsRepository
                    .getString(SettingsKeys.GOOGLE_ACCOUNT_PHOTO_URL)
                    .first()

                if (savedEmail.isNotEmpty()) {
                    _googleUserState.value = GoogleUserState(
                        isSignedIn = true,
                        email = savedEmail,
                        photoUrl = savedPhotoUrl.takeIf { it.isNotEmpty() }?.toUri()
                    )
                }

                Log.d(TAG, "restoreState: done")

            } catch (e: Exception) {
                Log.e(TAG, "restoreState: FAILED", e)
            }
        }
    }

    override suspend fun signIn(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse =
                credentialManager.getCredential(context, request)

            val credential = result.credential

            if (
                credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                val email = googleCredential.id
                val name = googleCredential.displayName
                val photo = googleCredential.profilePictureUri

                _googleUserState.value = GoogleUserState(
                    isSignedIn = true,
                    email = email,
                    name = name,
                    photoUrl = photo
                )

                settingsRepository.setString(SettingsKeys.GOOGLE_ACCOUNT_EMAIL, email)
                settingsRepository.setString(
                    SettingsKeys.GOOGLE_ACCOUNT_PHOTO_URL,
                    photo?.toString() ?: ""
                )

                Log.d(TAG, "signIn: SUCCESS $email")
                Result.success(email)

            } else {
                val error = Exception("Unexpected credential type: ${credential.type}")
                Log.e(TAG, "signIn: $error")
                Result.failure(error)
            }

        } catch (e: GetCredentialException) {
            Log.e(TAG, "signIn: Credential error", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "signIn: Unexpected error", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        _googleUserState.value = GoogleUserState()

        settingsRepository.setString(SettingsKeys.GOOGLE_ACCOUNT_EMAIL, "")
        settingsRepository.setString(SettingsKeys.GOOGLE_ACCOUNT_PHOTO_URL, "")

        val cachedFile = File(context.filesDir, "google_profile.jpg")
        if (cachedFile.exists()) cachedFile.delete()

        Log.d(TAG, "signOut: done")
    }

    override fun getAccountEmail(): String? = _googleUserState.value.email
}
