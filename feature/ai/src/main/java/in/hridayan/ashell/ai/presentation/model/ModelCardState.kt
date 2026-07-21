package `in`.hridayan.ashell.ai.presentation.model

/**
 * Display state for a model card in the AI Model Manager screen.
 */
enum class ModelCardState {
    /** Model is not downloaded */
    NOT_INSTALLED,

    /** Model is currently being downloaded */
    DOWNLOADING,

    /** Model is downloaded but not selected */
    INSTALLED,

    /** Model is downloaded and currently selected for inference */
    SELECTED,

    /** Download or verification failed */
    ERROR
}
