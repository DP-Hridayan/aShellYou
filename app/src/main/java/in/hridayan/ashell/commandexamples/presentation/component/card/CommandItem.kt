package `in`.hridayan.ashell.commandexamples.presentation.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.component.chip.LabelChip
import `in`.hridayan.ashell.commandexamples.presentation.component.dialog.EditCommandDialog
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandViewModel
import `in`.hridayan.ashell.core.presentation.ui.component.button.FavouriteIconButton
import `in`.hridayan.ashell.core.presentation.ui.component.card.CollapsibleCard
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens
import kotlinx.coroutines.launch

@Composable
fun CommandItem(
    viewModel: CommandViewModel = hiltViewModel(),
    modifier: Modifier,
    id: Int,
    command: String,
    description: String,
    isFavourite: Boolean,
    labels: List<String>
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }


    CollapsibleCard(
        modifier = modifier.padding(horizontal = Dimens.paddingLarge),
        collapsedContent = { modifier ->
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)) {
                    if (labels.isNotEmpty()) Labels(modifier = Modifier.weight(1f), labels = labels)
                    FavouriteButton(id = id, isFavourite = isFavourite, viewModel = viewModel)
                }

                Text(
                    text = command, style = MaterialTheme.typography.titleLarge
                )
            }
        },

        expandedContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
            ) {
                if (description.isNotEmpty()) {
                    Text(
                        text = description,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EditButton(id = id, viewModel = viewModel)
                    DeleteButton(id = id, viewModel = viewModel)
                }
            }
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

@Composable
fun DeleteButton(modifier: Modifier = Modifier, id: Int, viewModel: CommandViewModel) {
    Image(
        painter = painterResource(id = R.drawable.ic_delete),
        contentDescription = null,
        colorFilter = ColorFilter.Companion.tint(
            MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                viewModel.deleteCommand(id = id, onSuccess = {})
            })
    )
}

@Composable
fun EditButton(modifier: Modifier = Modifier, id: Int, viewModel: CommandViewModel) {
    var isEditDialogOpen by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    Image(
        painter = painterResource(id = R.drawable.ic_edit),
        contentDescription = null,
        colorFilter = ColorFilter.Companion.tint(
            MaterialTheme.colorScheme.onErrorContainer
        ),
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {
                coroutineScope.launch {
                    viewModel.setFieldsForEdit(id = id)
                    isEditDialogOpen = true
                }
            })
    )

    if (isEditDialogOpen) EditCommandDialog(id = id, onDismiss = { isEditDialogOpen = false })
}

@Composable
fun FavouriteButton(
    modifier: Modifier = Modifier,
    id: Int,
    isFavourite: Boolean,
    viewModel: CommandViewModel
) {
    FavouriteIconButton(isFavorite = isFavourite, onToggle = {
        viewModel.toggleFavourite(
            id = id,
            isFavourite = !isFavourite,
            onSuccess = {}
        )
    })
}