package `in`.hridayan.ashell.mirror.presentation.model

/** Everything the mirror screen's content can ask for, grouped so its layouts pass one value down. */
data class MirrorContentActions(
    val mirror: MirrorActions,
    val fullscreen: FullscreenActions,
    val quality: QualityActions,
    val onLeave: () -> Unit
)
