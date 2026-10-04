package `in`.hridayan.ashell.logcat.presentation.components.filter

import android.content.Context
import android.content.pm.PackageManager
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private const val PLACEHOLDER_INSET_FRACTION = 0.2f

/**
 * Decoded launcher icons, kept across openings so scrolling back does not decode again. Keyed by
 * package and pixel size, so a small icon is never stretched into a larger slot.
 */
private val iconCache = LruCache<String, ImageBitmap>(ICON_CACHE_ENTRIES)

/**
 * The launcher icon of an installed app. Icons are decoded off the main thread when their row is
 * first composed, so a list of several hundred apps never decodes them all up front. A package
 * glyph stands in while decoding, or when the app is not installed or has no icon.
 *
 * When [circular], the icon is clipped to a circle, which also trims legacy square icons, and the
 * glyph sits on a tinted circle of the same size.
 */
@Composable
fun AppIcon(
    packageName: String,
    size: Dp,
    modifier: Modifier = Modifier,
    circular: Boolean = false,
) {
    val context = LocalContext.current
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val icon by produceState(iconCache.get(cacheKey(packageName, sizePx)), packageName, sizePx) {
        if (value == null) value = loadIcon(context, packageName, sizePx)
    }

    val bitmap = icon
    when {
        bitmap != null -> Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = modifier
                .size(size)
                .then(if (circular) Modifier.clip(CircleShape) else Modifier),
        )

        circular -> CircularPlaceholder(size = size, modifier = modifier)
        else -> PackageGlyph(modifier = modifier.size(size))
    }
}

@Composable
private fun CircularPlaceholder(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        PackageGlyph(
            modifier = Modifier
                .fillMaxSize()
                .padding(size * PLACEHOLDER_INSET_FRACTION),
        )
    }
}

@Composable
private fun PackageGlyph(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(R.drawable.ic_package),
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

private fun cacheKey(packageName: String, sizePx: Int): String = "$packageName@$sizePx"

private suspend fun loadIcon(context: Context, packageName: String, sizePx: Int): ImageBitmap? =
    withContext(Dispatchers.IO) {
        try {
            context.packageManager.getApplicationIcon(packageName)
                .toBitmap(sizePx, sizePx)
                .asImageBitmap()
                .also { iconCache.put(cacheKey(packageName, sizePx), it) }
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
