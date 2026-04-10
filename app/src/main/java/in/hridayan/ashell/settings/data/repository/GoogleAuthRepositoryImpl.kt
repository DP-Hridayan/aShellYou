package `in`.hridayan.ashell.settings.data.repository

import android.content.Context
import android.net.Uri
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
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.settings.data.SettingsKeys
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
import java.io.File
import javax.inject.Inject

class GoogleAuthRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : GoogleAuthRepository {

    companion object {
        private const val TAG = "GoogleAuth"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isSignedIn = MutableStateFlow(false)
    override val isSignedIn: StateFlow<Boolean> = _isSignedIn.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    override val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    private val _userName = MutableStateFlow<String?>(null)
    override val userName: StateFlow<String?> = _userName.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow<Uri?>(null)
    override val userPhotoUrl: StateFlow<Uri?> = _userPhotoUrl.asStateFlow()

    init {
        // Restore sign-in state from persisted data — no API calls made here
        scope.launch {
            val savedEmail = settingsRepository.getString(SettingsKeys.GOOGLE_ACCOUNT_EMAIL).first()
            val savedPhotoUrl =
                settingsRepository.getString(SettingsKeys.GOOGLE_ACCOUNT_PHOTO_URL).first()
            Log.d(TAG, "init: restored email='$savedEmail', photoUrl='$savedPhotoUrl'")
            if (savedEmail.isNotEmpty()) {
                _userEmail.value = savedEmail
                _isSignedIn.value = true
                if (savedPhotoUrl.isNotEmpty()) {
                    _userPhotoUrl.value = savedPhotoUrl.toUri()
                }
            }
        }
    }

    override suspend fun signIn(context: Context): Result<String> {
        return try {
            val webClientId = context.getString(R.string.google_web_client_id)
            Log.d(TAG, "signIn: using webClientId = $webClientId")

            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d(TAG, "signIn: launching CredentialManager.getCredential()...")
            val result: GetCredentialResponse = credentialManager.getCredential(context, request)
            Log.d(TAG, "signIn: got credential response, type = ${result.credential.type}")

            val credential = result.credential
            when {
                credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {

                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val email = googleIdTokenCredential.id // email
                    val displayName = googleIdTokenCredential.displayName
                    val photoUri = googleIdTokenCredential.profilePictureUri

                    Log.d(
                        TAG,
                        "signIn: SUCCESS — email=$email, displayName=$displayName, photo=$photoUri"
                    )

                    _userEmail.value = email
                    _userName.value = displayName
                    _userPhotoUrl.value = photoUri
                    _isSignedIn.value = true
                    settingsRepository.setString(SettingsKeys.GOOGLE_ACCOUNT_EMAIL, email)
                    settingsRepository.setString(
                        SettingsKeys.GOOGLE_ACCOUNT_PHOTO_URL,
                        photoUri?.toString() ?: ""
                    )

                    Result.success(email)
                }

                else -> {
                    Log.e(TAG, "signIn: unexpected credential type: ${credential.type}")
                    Result.failure(Exception("Unexpected credential type: ${credential.type}"))
                }
            }
        } catch (e: GetCredentialException) {
            Log.e(TAG, "signIn: GetCredentialException — type=${e.type}, message=${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "signIn: unexpected exception", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        Log.d(TAG, "signOut: clearing state")
        _isSignedIn.value = false
        _userEmail.value = null
        _userName.value = null
        _userPhotoUrl.value = null
        settingsRepository.setString(SettingsKeys.GOOGLE_ACCOUNT_EMAIL, "")
        settingsRepository.setString(SettingsKeys.GOOGLE_ACCOUNT_PHOTO_URL, "")
        // Delete cached profile image
        val cachedFile = File(context.filesDir, "google_profile.jpg")
        if (cachedFile.exists()) cachedFile.delete()
    }

    override fun getAccountEmail(): String? = _userEmail.value
}
