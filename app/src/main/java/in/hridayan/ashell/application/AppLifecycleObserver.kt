package `in`.hridayan.ashell.application

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import `in`.hridayan.ashell.core.common.domain.model.AuthenticationTimeout
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLifecycleObserver @Inject constructor(
    private val settingsRepository: SettingsRepository
) : DefaultLifecycleObserver {

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    private var lastBackgroundTimeMs: Long = 0L
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        scope.launch {
            val requireAuth =
                settingsRepository.getBoolean(SettingsKeys.RequireAuthentication).first()

            if (requireAuth) {
                _isLocked.value = true
            }
        }
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)

        scope.launch {
            val requireAuth =
                settingsRepository.getBoolean(SettingsKeys.RequireAuthentication).first()

            if (!requireAuth) return@launch

            if (lastBackgroundTimeMs <= 0) {
                _isLocked.value = true
                return@launch
            }

            val timeInBackgroundMs = System.currentTimeMillis() - lastBackgroundTimeMs
            val timeoutValue = settingsRepository.getInt(SettingsKeys.AuthenticationTimeout).first()

            if (timeoutValue == AuthenticationTimeout.NEVER) return@launch

            val timeoutMs = timeoutValue * 60 * 1000L

            if (timeInBackgroundMs >= timeoutMs) {
                _isLocked.value = true
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        lastBackgroundTimeMs = System.currentTimeMillis()
    }

    fun unlock() {
        _isLocked.value = false
        lastBackgroundTimeMs = 0L
    }
}
