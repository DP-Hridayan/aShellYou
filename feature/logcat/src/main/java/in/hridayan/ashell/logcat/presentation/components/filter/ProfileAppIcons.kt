package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.presentation.model.appIconStack

private val ICON_SIZE = 20.dp
private val RING_WIDTH = 1.5.dp
private val ICON_OVERLAP = 6.dp
private val OVERFLOW_BLUR = 2.dp
private const val OVERFLOW_ICON_ALPHA = 0.5f
private const val OVERFLOW_SCRIM_ALPHA = 0.55f

/**
 * A profile's apps as small overlapping circles, each ringed in [ringColor] so neighbours stay
 * apart. Beyond three, the fourth is blurred under a "+N" counter; the blur needs Android 12, so
 * older versions only fade it. Read by screen readers as a single app count.
 */
@Composable
fun ProfileAppIcons(
    packages: Collection<String>,
    ringColor: Color,
    modifier: Modifier = Modifier,
) {
    val stack = appIconStack(packages)
    val label = pluralStringResource(R.plurals.app_count, packages.size, packages.size)

    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(-ICON_OVERLAP),
    ) {
        stack.shown.forEach { packageName ->
            RingedIcon(ringColor) { AppIcon(packageName = packageName, size = ICON_SIZE, circular = true) }
        }

        stack.overflowPackage?.let { packageName ->
            RingedIcon(ringColor) { OverflowIcon(packageName, stack.overflowCount, ringColor) }
        }
    }
}

@Composable
private fun RingedIcon(ringColor: Color, icon: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .background(ringColor, CircleShape)
            .padding(RING_WIDTH),
    ) {
        icon()
    }
}

@Composable
private fun OverflowIcon(packageName: String, count: Int, scrimColor: Color) {
    Box(modifier = Modifier.size(ICON_SIZE), contentAlignment = Alignment.Center) {
        AppIcon(
            packageName = packageName,
            size = ICON_SIZE,
            circular = true,
            modifier = Modifier
                .blur(OVERFLOW_BLUR, BlurredEdgeTreatment(CircleShape))
                .alpha(OVERFLOW_ICON_ALPHA),
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(scrimColor.copy(alpha = OVERFLOW_SCRIM_ALPHA), CircleShape),
        )

        AutoResizeableText(
            text = stringResource(R.string.overflow_count, count),
            style = MaterialTheme.typography.labelSmall,
            color = LocalContentColor.current,
            textAlign = TextAlign.Center,
        )
    }
}
