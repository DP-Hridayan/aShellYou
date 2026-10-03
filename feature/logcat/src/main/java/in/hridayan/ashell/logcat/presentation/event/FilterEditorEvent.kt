package `in`.hridayan.ashell.logcat.presentation.event

/** One-off outcomes of saving in the filter profile editor. */
sealed interface FilterEditorEvent {
    data object Saved : FilterEditorEvent
    data object SaveFailed : FilterEditorEvent
}
