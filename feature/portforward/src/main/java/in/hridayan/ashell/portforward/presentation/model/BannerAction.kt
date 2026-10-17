package `in`.hridayan.ashell.portforward.presentation.model

/** A button on a port forwarding banner. */
data class BannerAction(val label: String, val onClick: () -> Unit)
