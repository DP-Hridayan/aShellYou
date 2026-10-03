package `in`.hridayan.ashell.mirror.domain.model

data class VideoSize(val width: Int, val height: Int) {
    val isLandscape: Boolean get() = width > height
}
