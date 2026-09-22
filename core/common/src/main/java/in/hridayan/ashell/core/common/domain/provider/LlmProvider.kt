package `in`.hridayan.ashell.core.common.domain.provider

import androidx.compose.runtime.Immutable

/**
 * Represents a supported cloud LLM provider for command analysis.
 *
 * The order of [all] is load-bearing: it decides which provider is adopted when the active
 * provider's API key is deleted while other providers still hold one.
 *
 * @param id Stable identifier stored in settings persistence.
 * @param displayName Human-readable name shown in the UI.
 */
@Immutable
sealed class LlmProvider(val id: String, val displayName: String) {
    data object Gemini : LlmProvider("gemini", "Gemini (Google)")

    data object Groq : LlmProvider("groq", "Groq")

    data object OpenRouter : LlmProvider("openrouter", "OpenRouter")

    companion object {
        val all: List<LlmProvider> by lazy { listOf(Gemini, Groq, OpenRouter) }

        fun fromId(id: String): LlmProvider? = all.firstOrNull { it.id == id }
    }
}
