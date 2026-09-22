package `in`.hridayan.ashell.settings.domain.usecase

import android.util.Log
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.model.ai.ModelTier
import `in`.hridayan.ashell.core.common.domain.provider.LlmModelCatalog
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.usecase.ai.GetActiveLlmProviderUseCase
import `in`.hridayan.ashell.core.common.domain.usecase.ai.ModelFallbackExecutor
import `in`.hridayan.ashell.core.presentation.theme.data.ColorSchemePayload
import kotlinx.serialization.json.Json
import javax.inject.Inject

private const val TAG = "GenerateCustomTheme"

class GenerateCustomThemeUseCase @Inject constructor(
    private val clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
    private val apiKeyRepository: ApiKeyRepository,
    private val modelCatalog: LlmModelCatalog,
    private val fallbackExecutor: ModelFallbackExecutor,
    private val getActiveLlmProvider: GetActiveLlmProviderUseCase,
) {
    suspend operator fun invoke(prompt: String, fallbackModels: List<String>? = null): Result<ColorSchemePayload> {
        return try {
            val provider = getActiveLlmProvider() ?: throw CloudNetworkException.NoActiveProvider()
            val client = clients[provider]
                ?: throw CloudNetworkException.ProviderNotConfigured(provider)
            val apiKey = apiKeyRepository.getKey(provider)?.takeIf { it.isNotBlank() }
                ?: throw CloudNetworkException.ProviderNotConfigured(provider)

            val models = fallbackModels?.takeIf { it.isNotEmpty() }
                ?: modelCatalog.chain(provider, ModelTier.QUALITY, toolsRequired = false)

            if (models.isEmpty()) throw CloudNetworkException.ProviderNotConfigured(provider)

            val systemPrompt = """
                You are a Material Design 3 theme generator expert. You will generate a complete, mathematically precise Material 3 ColorScheme based on the user's creative prompt.
                
                RULES:
                1. You must output ONLY a valid JSON object matching the exact schema provided. No markdown, no intro, no trailing text.
                2. ALL color values must be in exactly 6-character Hex format (e.g., "FF5252" or "000000"). Do NOT include the '#' symbol.
                3. CONTRAST RULES (CRITICAL): 
                   - Any `on[Color]` (e.g., `onPrimary`, `onSurface`) must have at least a 4.5:1 contrast ratio against its base color (`primary`, `surface`).
                   - `primaryContainer` must be visually distinct from `primary`. `onPrimaryContainer` must have at least a 3.0:1 contrast against `primaryContainer`.
                4. The "name" field should be a creative, 2-3 word name for the theme based on the user's prompt.
                5. CRITICAL: The "isDarkTheme" field MUST be boolean `true` if the theme you generated relies on dark backgrounds, or `false` if it relies on light backgrounds. DO NOT blindly copy the schema example value! Analyze the colors you picked.
                
                JSON SCHEMA:
                {
                  "name": "Creative Theme Name",
                  "primary": "XXXXXX",
                  "onPrimary": "XXXXXX",
                  "primaryContainer": "XXXXXX",
                  "onPrimaryContainer": "XXXXXX",
                  "inversePrimary": "XXXXXX",
                  "secondary": "XXXXXX",
                  "onSecondary": "XXXXXX",
                  "secondaryContainer": "XXXXXX",
                  "onSecondaryContainer": "XXXXXX",
                  "tertiary": "XXXXXX",
                  "onTertiary": "XXXXXX",
                  "tertiaryContainer": "XXXXXX",
                  "onTertiaryContainer": "XXXXXX",
                  "error": "XXXXXX",
                  "onError": "XXXXXX",
                  "errorContainer": "XXXXXX",
                  "onErrorContainer": "XXXXXX",
                  "background": "XXXXXX",
                  "onBackground": "XXXXXX",
                  "surface": "XXXXXX",
                  "onSurface": "XXXXXX",
                  "surfaceVariant": "XXXXXX",
                  "onSurfaceVariant": "XXXXXX",
                  "surfaceTint": "XXXXXX",
                  "inverseSurface": "XXXXXX",
                  "inverseOnSurface": "XXXXXX",
                  "outline": "XXXXXX",
                  "outlineVariant": "XXXXXX",
                  "scrim": "000000",
                  "surfaceBright": "XXXXXX",
                  "surfaceDim": "XXXXXX",
                  "surfaceContainer": "XXXXXX",
                  "surfaceContainerHigh": "XXXXXX",
                  "surfaceContainerHighest": "XXXXXX",
                  "surfaceContainerLow": "XXXXXX",
                  "surfaceContainerLowest": "XXXXXX",
                  "isDarkTheme": true
                }
            """.trimIndent()

            val response = fallbackExecutor.execute(provider, models) { model ->
                client.complete(
                    model = model,
                    systemPrompt = systemPrompt,
                    userPrompt = "Generate a theme based on this prompt: $prompt",
                    apiKey = apiKey
                )
            }

            Log.d(TAG, "Raw AI Response:\n$response")

            // Sanitize response to ensure it's pure JSON
            val jsonString = response.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$jsonString}"

            Log.d(TAG, "Cleaned JSON:\n$finalJson")

            val jsonParser = Json { ignoreUnknownKeys = true }
            val payload = jsonParser.decodeFromString<ColorSchemePayload>(finalJson)

            Result.success(payload)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate or parse theme", e)
            Result.failure(e)
        }
    }
}
