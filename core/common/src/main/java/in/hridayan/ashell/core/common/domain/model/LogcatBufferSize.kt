package `in`.hridayan.ashell.core.common.domain.model

private const val BYTES_PER_MEGABYTE = 1024L * 1024L

/**
 * Selectable memory budgets for the logcat buffer, expressed in megabytes.
 *
 * Every option is large enough to hold several minutes of logs from a busy device, so no
 * selection can starve the buffer into evicting entries the user is still looking at.
 * [sanitize] guarantees that property for values arriving from a restored backup or an
 * older install.
 */
object LogcatBufferSize {
    const val SMALL = 4
    const val DEFAULT = 10
    const val LARGE = 20
    const val VERY_LARGE = 40

    val ALL: List<Int> = listOf(SMALL, DEFAULT, LARGE, VERY_LARGE)

    fun sanitize(megabytes: Int): Int = if (megabytes in ALL) megabytes else DEFAULT

    fun toBytes(megabytes: Int): Long = sanitize(megabytes) * BYTES_PER_MEGABYTE
}
