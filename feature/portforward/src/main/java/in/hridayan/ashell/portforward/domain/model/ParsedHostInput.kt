package `in`.hridayan.ashell.portforward.domain.model

/**
 * What the user typed or pasted into the address field, split into parts.
 *
 * @property port null when the input named no port and its scheme has no default.
 * @property isHttp the input was an http or https URL.
 */
data class ParsedHostInput(
    val host: String,
    val port: Int?,
    val path: String?,
    val isHttp: Boolean
)
