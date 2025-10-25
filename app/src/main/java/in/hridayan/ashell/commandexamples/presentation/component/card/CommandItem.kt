@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.component.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.component.chip.LabelChip
import `in`.hridayan.ashell.commandexamples.presentation.component.dialog.EditCommandDialog
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.button.FavouriteIconButton
import `in`.hridayan.ashell.core.presentation.components.card.CollapsibleCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel
import kotlinx.coroutines.launch

@Composable
fun CommandItem(
    modifier: Modifier,
    id: Int,
    command: String,
    description: String,
    isFavourite: Boolean,
    labels: List<String>,
    viewModel: CommandViewModel = hiltViewModel(),
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val navController = LocalNavController.current

    CollapsibleCard(
        modifier = modifier.padding(horizontal = Dimens.paddingLarge),
        collapsedContent = { modifier ->
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
            ) {
                if (labels.isNotEmpty()) Labels(modifier = Modifier.fillMaxWidth(), labels = labels)

                Text(
                    text = command, style = MaterialTheme.typography.titleLargeEmphasized
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
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EditButton(id = id, viewModel = viewModel)
                    DeleteButton(id = id, viewModel = viewModel)
                    UseCommandButton(onClick = {
                        shellViewModel.onCommandChange(
                            TextFieldValue(
                                command
                            )
                        )

                        navController.popBackStack()
                    })
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

@Composable
fun UseCommandButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val weakHaptic = LocalWeakHaptic.current
    val size = ButtonDefaults.ExtraSmallContainerHeight

    Button(
        onClick = {
            weakHaptic()
            onClick()
        },
        modifier = modifier.heightIn(size),
        contentPadding = ButtonDefaults.contentPaddingFor(size)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_open_in_new),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(size))
        )

        Spacer(Modifier.widthIn(ButtonDefaults.iconSpacingFor(size)))

        AutoResizeableText(text = stringResource(R.string.use),)
    }
}