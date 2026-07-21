@file:OptIn(ExperimentalFoundationApi::class)

package `in`.hridayan.ashell.logcat.presentation.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.hridayan.ashell.logcat.domain.model.LogEntry
import `in`.hridayan.ashell.core.common.LocalDarkMode

/**
 * A single logcat row in the LazyColumn.
 *
 * Layout:
 *   [ 4dp color bar ] | [ Tag column weight=0.3 ] | [ Message column weight=0.7 ]
 *
 * Default: single line with ellipsis.
 * Expanded (tap): full text, animates open via [animateContentSize].
 * Long-press: triggers [onLongClick] to show the detail bottom sheet.
 */
@Composable
fun LogEntryRow(
    entry: LogEntry,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalDarkMode.current
    val barColor = LogLevelColors.barColor(entry.level)
    val bgColor = LogLevelColors.rowBackground(entry.level)
    val textColor = LogLevelColors.textColor(entry.level, isDark)
    val maxLines = if (isExpanded) Int.MAX_VALUE else 1

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .animateContentSize(animationSpec = tween(durationMillis = 200))
            .height(IntrinsicSize.Min)
    ) {
        // 4dp left color bar — always full height of the row
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(barColor)
        )

        // Tag column — weight 0.3, never bleeds into message column
        Column(
            modifier = Modifier
                .weight(0.3f)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = entry.tag,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = textColor,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }

        // Message column — weight 0.7
        Column(
            modifier = Modifier
                .weight(0.7f)
                .padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            Text(
                text = entry.message,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.87f),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

