package `in`.hridayan.ashell.portforward.presentation.model

/** What the detail sheet can ask for, hoisted to the screen. */
data class TunnelDetailActions(
    val onRunningChange: (Boolean) -> Unit,
    val onEdit: () -> Unit,
    val onOpen: () -> Unit,
    val onCopyActivity: (String) -> Unit,
    val onDelete: () -> Unit,
    val onDismiss: () -> Unit
)
