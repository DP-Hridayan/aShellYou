package `in`.hridayan.ashell.commandexamples.presentation.component.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import `in`.hridayan.ashell.commandexamples.presentation.component.chip.LabelChip
import `in`.hridayan.ashell.core.presentation.ui.component.card.CollapsibleCard
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens

@Composable
fun CommandItem(
    modifier: Modifier, command: String, example: String, description: String, labels: List<String>
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    CollapsibleCard(
        modifier = modifier.padding(horizontal = Dimens.paddingLarge),
        collapsedContent = { modifier ->
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
            ) {
                if (labels.isNotEmpty()) Labels(labels = labels)

                Text(
                    text = command, style = MaterialTheme.typography.titleLarge
                )
            }
        },

        expandedContent = {
            Text(
                text = description,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = example,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        onStateChanged = { expanded ->
            isExpanded = expanded
        })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Labels(modifier: Modifier = Modifier, labels: List<String>) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium),
        verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
    ) {
        labels.forEach {
            LabelChip(label = it)
        }
    }
}