package `in`.hridayan.ashell.settings.domain.usecase

import android.util.Log
import `in`.hridayan.ashell.core.common.SettingsKeys
import `in`.hridayan.ashell.core.common.domain.model.CloudNetworkException
import `in`.hridayan.ashell.core.common.domain.provider.LlmProvider
import `in`.hridayan.ashell.core.common.domain.provider.LlmProviderClient
import `in`.hridayan.ashell.core.common.domain.repository.ApiKeyRepository
import `in`.hridayan.ashell.core.common.domain.repository.SettingsRepository
import `in`.hridayan.ashell.core.presentation.theme.data.ColorSchemePayload
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import javax.inject.Inject
import `in`.hridayan.ashell.core.common.constants.AiModelConstants

class GenerateCustomThemeUseCase @Inject constructor(
    private val clients: Map<LlmProvider, @JvmSuppressWildcards LlmProviderClient>,
    private val apiKeyRepository: ApiKeyRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(prompt: String, fallbackModels: List<String>? = null): Result<ColorSchemePayload> {
        return try {
            val providerId = settingsRepository.getString(SettingsKeys.AiCloudProvider).firstOrNull() ?: SettingsKeys.AiCloudProvider.default
            val provider = LlmProvider.fromId(providerId) ?: LlmProvider.Gemini
            val client = clients[provider] ?: throw IllegalStateException("${provider.displayName} client not found")
            val apiKey = apiKeyRepository.getKey(provider)
                ?: throw IllegalStateException("${provider.displayName} API Key not set. Please set it in Settings -> AI Models -> Cloud Models.")

            val models = fallbackModels?.takeIf { it.isNotEmpty() } ?: when (provider) {
                LlmProvider.Gemini -> AiModelConstants.geminiModelsHighestToLowest
                // Add other providers here later
            }
            
            if (models.isEmpty()) throw IllegalStateException("No models configured for provider ${provider.displayName}")

            val systemPrompt = """
                You are a Material Design 3 theme generator expert. You will generate a complete, mathematically precise Material 3 ColorScheme based on the user's creative prompt.
                
                RULES:
                1. You must output ONLY a valid JSON object matching the exact schema provided. No markdown, no intro, no trailing text.
                2. ALL color values must be in exactly 6-character Hex format (e.g., "FF5252" or "000000"). Do NOT include the '#' symbol.
                3. CONTRAST RULES (CRITICAL): 
                   - Any `on[Color]` (e.g., `onPrimary`, `onSurface`) must have at least a 4.5:1 contrast ratio against its base color (`primary`, `surface`).
                   - `primaryContainer` must be visually distinct from `primary`. `onPrimaryContainer` must have at least a 3.0:1 contrast against `primaryContainer`.
                4. The "name" field should be a creative, 2-3 word name for the theme based on the user's prompt.
                5. The "svgPathData" field MUST contain a valid SVG `d` path string representing a **simple, symbolic, minimalist emblem or icon** that represents the theme. 
                   - For complex subjects (like Iron Man, Naruto, or movies), do NOT try to draw a detailed character or face. Instead, generate a highly recognizable minimalist symbol (e.g., an Arc Reactor or helmet outline for Iron Man, a Leaf Village symbol or Kunai for Naruto, a sword for a knight).
                   - The path must be a single continuous or compound shape designed for a 24x24 or 100x100 viewBox.
                   - Do not include `<svg>` or `<path>` tags, ONLY the raw mathematical `d` string (e.g., "M12 2L2 22h20L12 2z"). 
                   - If you cannot confidently generate a recognizable simple symbol, return an empty string.
                6. CRITICAL: The "isDarkTheme" field MUST be boolean `true` if the theme you generated relies on dark backgrounds, or `false` if it relies on light backgrounds. DO NOT blindly copy the schema example value! Analyze the colors you picked.
                
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
                  "svgPathData": "M12 2L2 22h20L12 2z",
                  "isDarkTheme": true
                }
            """.trimIndent()

            var lastException: Exception? = null
            var response = ""

            for (model in models) {
                try {
                    response = client.complete(
                        model = model,
                        systemPrompt = systemPrompt,
                        userPrompt = "Generate a theme based on this prompt: $prompt",
                        apiKey = apiKey
                    )
                    // If complete returns successfully, break out of loop
                    break
                } catch (e: CloudNetworkException.RateLimited) {
                    lastException = e
                  Log.w("GenerateCustomTheme", "Model $model rate limited, trying next")
                } catch (e: CloudNetworkException.ServerError) {
                    if (e.code == 429 || e.code >= 500 || e.code == 404) {
                        lastException = e
                        Log.w("GenerateCustomTheme", "Model $model server error ${e.code}, trying next")
                    } else {
                        throw e
                    }
                } catch (e: CloudNetworkException.NetworkError) {
                    lastException = e
                    Log.w("GenerateCustomTheme", "Model $model network/timeout error, trying next")
                } catch (e: CloudNetworkException.ParseError) {
                    lastException = e
                    Log.w("GenerateCustomTheme", "Model $model parse error, trying next")
                } catch (e: CloudNetworkException) {
                    // Fatal errors like Unauthorized
                    throw e
                }
            }
            
            if (response.isBlank()) {
                throw lastException ?: IllegalStateException("Failed to generate theme with all fallback models.")
            }

            Log.d("GenerateCustomTheme", "Raw AI Response:\n$response")

            // Sanitize response to ensure it's pure JSON
            val jsonString = response.substringAfter("{").substringBeforeLast("}")
            val finalJson = "{$jsonString}"
            
            Log.d("GenerateCustomTheme", "Cleaned JSON:\n$finalJson")

            val jsonParser = Json { ignoreUnknownKeys = true }
            val payload = jsonParser.decodeFromString<ColorSchemePayload>(finalJson)

            Result.success(payload)
        } catch (e: Exception) {
            Log.e("GenerateCustomTheme", "Failed to generate or parse theme", e)
            Result.failure(e)
        }
    }
}
