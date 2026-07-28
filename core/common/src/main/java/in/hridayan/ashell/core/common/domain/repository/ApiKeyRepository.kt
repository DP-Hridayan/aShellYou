package `in`.hridayan.ashell.core.common.domain.repository

import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import kotlinx.coroutines.flow.Flow

/**
 * Secure storage contract for per-provider LLM API keys.
 *
 * Implementations must encrypt keys at rest. Keys must never be logged.
 */
interface ApiKeyRepository {

    /** Encrypt and persist [key] for [provider]. Replaces any existing key. */
    fun setKey(provider: LlmProvider, key: String)

    /** Returns the decrypted key for [provider], or `null` if none is stored. */
    fun getKey(provider: LlmProvider): String?

    /** Permanently removes the stored key for [provider]. */
    fun deleteKey(provider: LlmProvider)

    /** Emits `true` whenever a key for [provider] is present; `false` after deletion. */
    fun hasKey(provider: LlmProvider): Flow<Boolean>
}
