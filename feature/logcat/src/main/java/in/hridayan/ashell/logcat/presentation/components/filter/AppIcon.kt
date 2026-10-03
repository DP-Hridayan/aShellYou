package `in`.hridayan.ashell.logcat.presentation.components.filter

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toBitmap
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val ICON_CACHE_ENTRIES = 256

/** Decoded launcher icons, kept across picker openings so scrolling back does not decode again. */
private val iconCache = LruCache<String, ImageBitmap>(ICON_CACHE_ENTRIES)

/**
 * The launcher icon of an installed app. Icons are decoded off the main thread when their row is
 * first composed, so a list of several hundred apps never decodes them all up front. A package
 * glyph stands in while decoding, or when the app has no icon.
 */
@Composable
fun AppIcon(
    packageName: String,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val icon by produceState(iconCache.get(packageName), packageName, sizePx) {
        if (value == null) value = loadIcon(context, packageName, sizePx)
    }

    val bitmap = icon
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier.size(size))
    } else {
        Icon(
            painter = painterResource(R.drawable.ic_package),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.size(size),
        )
    }
}

private suspend fun loadIcon(context: Context, packageName: String, sizePx: Int): ImageBitmap? =
    withContext(Dispatchers.IO) {
        try {
            context.packageManager.getApplicationIcon(packageName)
                .toBitmap(sizePx, sizePx)
                .asImageBitmap()
                .also { iconCache.put(packageName, it) }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
