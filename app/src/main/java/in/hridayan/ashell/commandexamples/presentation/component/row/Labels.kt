package `in`.hridayan.ashell.commandexamples.presentation.component.row

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.hridayan.ashell.commandexamples.presentation.component.chip.LabelChip
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Labels(
    modifier: Modifier = Modifier,
    labels: List<String>,
    showCrossIcon: Boolean = false,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
    ) {
        labels.forEach {
            LabelChip(
                label = it,
                showCrossIcon = showCrossIcon
            )
        }
    }
}