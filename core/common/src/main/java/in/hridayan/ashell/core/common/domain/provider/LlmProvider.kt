package `in`.hridayan.ashell.core.common.domain.provider

import androidx.compose.runtime.Immutable

/**
 * Represents a supported cloud LLM provider for command analysis.
 *
 * @param id Stable identifier stored in settings persistence.
 * @param displayName Human-readable name shown in the UI.
 */
@Immutable
sealed class LlmProvider(val id: String, val displayName: String) {
    data object Gemini : LlmProvider("gemini", "Gemini (Google)")

    companion object {
        val all: List<LlmProvider> by lazy { listOf(Gemini) }

        fun fromId(id: String): LlmProvider? = all.firstOrNull { it.id == id }
    }
}
