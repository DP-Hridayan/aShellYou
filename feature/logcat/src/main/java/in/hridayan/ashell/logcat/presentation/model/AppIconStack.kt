package `in`.hridayan.ashell.logcat.presentation.model

private const val VISIBLE_APP_ICONS = 3
private const val MAX_OVERFLOW_COUNT = 99

/**
 * How a profile's apps are drawn on its card: up to three icons, then, when there are more, the
 * next app's icon dimmed under a counter of every app not shown in full.
 */
data class AppIconStack(
    val shown: List<String>,
    val overflowPackage: String?,
    val overflowCount: Int,
)

/** Capped at 99 so the counter fits inside a small icon. */
fun appIconStack(packages: Collection<String>): AppIconStack {
    val hidden = packages.drop(VISIBLE_APP_ICONS)
    return AppIconStack(
        shown = packages.take(VISIBLE_APP_ICONS),
        overflowPackage = hidden.firstOrNull(),
        overflowCount = hidden.size.coerceAtMost(MAX_OVERFLOW_COUNT),
    )
}
