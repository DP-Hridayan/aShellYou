package `in`.hridayan.ashell.commandexamples.presentation.component.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.hridayan.ashell.commandexamples.presentation.component.chip.LabelChip
import `in`.hridayan.ashell.core.presentation.theme.Dimens
import kotlin.math.max

/**
 * This composable shows a flow row of label chips
 * @param modifier Modifier to be applied to the Labels Row
 * @param labels List of labels which is to be displayed in the row
 * @param showCrossIcon This boolean is to enable the option to add a 'x' Icon Button next to each individual label
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Labels(
    modifier: Modifier = Modifier,
    labels: List<String>,
    showCrossIcon: Boolean = false,
    crossIconOnClick: (label: String) -> Unit = {},
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    maxLines: Int = Int.MAX_VALUE
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall),
        maxLines = maxLines,
        maxItemsInEachRow = maxItemsInEachRow
    ) {
        labels.forEach {
            LabelChip(
                label = it,
                showCrossIcon = showCrossIcon,
                crossIconOnClick = crossIconOnClick
            )
        }
    }
}