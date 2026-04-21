package `in`.hridayan.ashell.settings.data.repository

import `in`.hridayan.ashell.settings.domain.model.GoogleUserState
import `in`.hridayan.ashell.settings.domain.repository.GoogleAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/** No-op implementation used in the fdroid flavor — Google Auth is not available. */
class FdroidGoogleAuthRepositoryImpl @Inject constructor() : GoogleAuthRepository {

    override val isAvailable: Boolean = false

    private val _googleUserState = MutableStateFlow(GoogleUserState())
    override val googleUserState: StateFlow<GoogleUserState> = _googleUserState.asStateFlow()

    override suspend fun signIn(): Result<String> =
        Result.failure(UnsupportedOperationException("Google Sign-In is not available in this build."))

    override suspend fun signOut() {
        // no-op
    }

    override fun getAccountEmail(): String? = null
}
