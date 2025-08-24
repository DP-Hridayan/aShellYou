package `in`.hridayan.ashell.core.presentation.components.scrollbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset

@Composable
fun VerticalScrollbar(
    modifier: Modifier = Modifier,
    listState: LazyListState
) {
    val layoutInfo by remember {
        derivedStateOf { listState.layoutInfo }
    }

    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo.size

    if (totalItems == 0 || visibleItems == 0) return

    val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

    val (scrollProgress, thumbFraction) = remember(listState, totalItems, visibleItems, viewportHeight) {
        derivedStateOf {
            val thumbFraction = visibleItems.toFloat() / totalItems.toFloat()
            val totalScrollableItems = (totalItems - visibleItems).coerceAtLeast(1)
            val scrollProgress = listState.firstVisibleItemIndex / totalScrollableItems.toFloat()
            scrollProgress.coerceIn(0f, 1f) to thumbFraction
        }
    }.value

    val thumbHeightPx = (viewportHeight * thumbFraction).toInt()

    val thumbOffsetY = (scrollProgress * (viewportHeight - thumbHeightPx)).toInt()

    if (layoutInfo.totalItemsCount <= layoutInfo.visibleItemsInfo.size) return

    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), RoundedCornerShape(50))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(with(LocalDensity.current) { thumbHeightPx.toDp() })
                .align(Alignment.TopStart)
                .offset { IntOffset(0, thumbOffsetY) }
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
        )
    }
}
