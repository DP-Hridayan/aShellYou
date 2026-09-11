package `in`.hridayan.ashell.core.shizuku.data

import android.os.Bundle
import `in`.hridayan.ashell.core.shizuku.domain.ShizukuBinderSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku

private const val PLUS_KEY_PREFIX = "af.shizuku"
private const val PLUS_RIKKA_EXTRA_BINDER = "rikka.shizuku.intent.extra.BINDER"

/**
 * Decides which Shizuku server's binder the app keeps when several servers push one.
 * Stock Shizuku always wins; a Shizuku+ binder is only accepted while no live stock binder exists.
 * Stock pushes a single `moe.*` extra, Shizuku+ pushes `af.*` and `rikka.*` extras as well.
 */
object ShizukuBinderArbiter {

    private val _currentSource = MutableStateFlow(ShizukuBinderSource.NONE)
    val currentSource: StateFlow<ShizukuBinderSource> = _currentSource.asStateFlow()

    @Volatile
    private var deathListenerRegistered = false

    fun classify(extras: Bundle): ShizukuBinderSource {
        val keys = runCatching { extras.keySet() }.getOrNull() ?: return ShizukuBinderSource.UNKNOWN
        val fromPlus = keys.any { it.startsWith(PLUS_KEY_PREFIX) || it == PLUS_RIKKA_EXTRA_BINDER }
        return if (fromPlus) ShizukuBinderSource.PLUS else ShizukuBinderSource.STOCK
    }

    fun shouldAccept(source: ShizukuBinderSource, binderAlive: Boolean = isBinderAlive()): Boolean {
        if (source != ShizukuBinderSource.PLUS) return true
        return !(binderAlive && _currentSource.value == ShizukuBinderSource.STOCK)
    }

    fun onAccepted(source: ShizukuBinderSource) {
        ensureDeathListener()
        _currentSource.value = source
    }

    fun onBinderLost() {
        _currentSource.value = ShizukuBinderSource.NONE
    }

    private fun isBinderAlive(): Boolean = runCatching { Shizuku.pingBinder() }.getOrDefault(false)

    private fun ensureDeathListener() {
        if (deathListenerRegistered) return
        deathListenerRegistered = true
        runCatching { Shizuku.addBinderDeadListener { onBinderLost() } }
    }
}
