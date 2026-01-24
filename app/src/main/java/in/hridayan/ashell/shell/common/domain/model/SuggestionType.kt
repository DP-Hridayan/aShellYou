package `in`.hridayan.ashell.shell.common.domain.model

/**
 * Defines the type of suggestion being offered to the user in the shell screen.
 *
 * This helps in categorizing suggestions and potentially changing how they are
 * displayed or processed.
 */
enum class SuggestionType {
    /**
     * Represents a shell command (e.g., 'ls', 'cd', 'pm').
     */
    COMMAND,

    /**
     * Represents an Android package name (e.g., 'com.android.settings').
     */
    PACKAGE,

    /**
     * Represents an Android permission (e.g., 'android.permission.INTERNET').
     */
    PERMISSION
}
