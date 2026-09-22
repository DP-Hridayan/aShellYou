package `in`.hridayan.ashell.ai.data.remote.catalog.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class OpenRouterCatalogResponse(
    val data: List<OpenRouterModelDto> = emptyList(),
)

@Serializable
internal data class OpenRouterModelDto(
    val id: String,
    @SerialName("context_length") val contextLength: Long? = null,
    val pricing: OpenRouterPricingDto? = null,
    @SerialName("supported_parameters") val supportedParameters: List<String> = emptyList(),
)

/** Prices are decimal strings such as `"0"` or `"0.00000435"`, per token. */
@Serializable
internal data class OpenRouterPricingDto(
    val prompt: String? = null,
    val completion: String? = null,
)
