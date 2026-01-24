package `in`.hridayan.ashell.shell.common.domain.model

/**
 * Labels used to categorize suggestions based on their origin or classification.
 */
enum class SuggestionLabel {
    /**
     * Indicates a system-level suggestion (e.g., system package or command).
     */
    SYSTEM,

    /**
     * Indicates a user-level suggestion (e.g., user-installed package).
     */
    USER
}
