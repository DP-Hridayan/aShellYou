package `in`.hridayan.ashell.ai.presentation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.resources.R

/**
 * The walkthrough shown when a provider's "Get API key" section is expanded.
 *
 * @param url Opened when the user taps the link.
 * @param displayUrl The shorter form written into the first step's text, which is what gets styled
 * as the tappable link.
 * @param firstStepRes Takes the display URL as its single format argument.
 */
@Immutable
data class ApiKeyHelp(
    val url: String,
    val displayUrl: String,
    @param:StringRes val firstStepRes: Int,
    @param:StringRes val remainingStepRes: List<Int>,
)

@Composable
fun apiKeyHelpFor(provider: LlmProvider): ApiKeyHelp = remember(provider.id) {
    when (provider) {
        LlmProvider.Gemini -> ApiKeyHelp(
            url = UrlConst.URL_GOOGLE_GEMINI_API_KEY,
            displayUrl = UrlConst.URL_GOOGLE_AI_STUDIO,
            firstStepRes = R.string.gemini_api_key_step_1,
            remainingStepRes = listOf(
                R.string.gemini_api_key_step_2,
                R.string.gemini_api_key_step_3,
                R.string.gemini_api_key_step_4,
            ),
        )

        LlmProvider.Groq -> ApiKeyHelp(
            url = UrlConst.URL_GROQ_API_KEY,
            displayUrl = UrlConst.URL_GROQ_CONSOLE,
            firstStepRes = R.string.groq_api_key_step_1,
            remainingStepRes = listOf(
                R.string.groq_api_key_step_2,
                R.string.groq_api_key_step_3,
                R.string.groq_api_key_step_4,
            ),
        )

        LlmProvider.OpenRouter -> ApiKeyHelp(
            url = UrlConst.URL_OPENROUTER_API_KEY,
            displayUrl = UrlConst.URL_OPENROUTER,
            firstStepRes = R.string.openrouter_api_key_step_1,
            remainingStepRes = listOf(
                R.string.openrouter_api_key_step_2,
                R.string.openrouter_api_key_step_3,
                R.string.openrouter_api_key_step_4,
            ),
        )
    }
}
