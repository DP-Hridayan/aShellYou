package `in`.hridayan.ashell.ai.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents the danger level of a shell/ADB command.
 * Used to visually indicate risk to the user.
 */
@Serializable
enum class DangerLevel {
    /** Command is safe and has no destructive side effects */
    SAFE,

    /** Command has minor side effects that are easily reversible */
    LOW_RISK,

    /** Command modifies system state and may require attention */
    MODERATE,

    /** Command can cause significant damage or data loss */
    DANGEROUS,

    /** Command can brick the device, wipe all data, or cause irreversible harm */
    CRITICAL
}
