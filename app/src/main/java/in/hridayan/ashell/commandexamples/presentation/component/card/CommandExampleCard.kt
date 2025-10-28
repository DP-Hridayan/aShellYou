@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.commandexamples.presentation.component.card

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.component.dialog.EditCommandDialog
import `in`.hridayan.ashell.commandexamples.presentation.component.row.Labels
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandExamplesViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.button.FavouriteIconButton
import `in`.hridayan.ashell.core.presentation.components.card.CollapsibleCard
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.ClipboardUtils
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.shell.presentation.viewmodel.ShellViewModel
import kotlinx.coroutines.launch

@Composable
fun CommandExampleCard(
    modifier: Modifier,
    id: Int,
    command: String,
    description: String,
    isFavourite: Boolean,
    labels: List<String>,
    shellViewModel: ShellViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val interactionSources = remember { List(3) { MutableInteractionSource() } }

    CollapsibleCard(
        modifier = modifier,
        collapsedContent = {
            if (labels.isNotEmpty()) Labels(modifier = Modifier.fillMaxWidth(), labels = labels)

            Text(
                text = command,
                style = MaterialTheme.typography.titleMediumEmphasized,
                modifier = Modifier.padding(start = 5.dp)
            )
        },

        expandedContent = {
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                @Suppress("DEPRECATION")
                ButtonGroup(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    EditButton(
                        id = id,
                        interactionSource = interactionSources[0],
                        modifier = Modifier
                            .size(40.dp)
                            .animateWidth(interactionSources[0])
                    )
                    DeleteButton(
                        id = id,
                        interactionSource = interactionSources[1],
                        modifier = Modifier
                            .size(40.dp)
                            .animateWidth(interactionSources[1])
                    )
                    CopyButton(
                        id = id,
                        interactionSource = interactionSources[2],
                        modifier = Modifier
                            .size(40.dp)
                            .animateWidth(interactionSources[2])
                    )
                }

                UseCommandButton(onClick = {
                    shellViewModel.onCommandChange(
                        TextFieldValue(
                            command
                        )
                    )

                    navController.popBackStack()
                })
            }
        })
}

@Composable
private fun DeleteButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    id: Int,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current

    IconButton(
        onClick = {
            weakHaptic()
            viewModel.deleteCommand(id = id, onSuccess = {})
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shapes = IconButtonDefaults.shapes(),
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_delete),
            contentDescription = null,
        )
    }
}

@Composable
private fun EditButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    id: Int,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    var isEditDialogOpen by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val weakHaptic = LocalWeakHaptic.current

    IconButton(
        onClick = {
            weakHaptic()
            coroutineScope.launch {
                viewModel.setFieldsForEdit(id = id)
                isEditDialogOpen = true
            }
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        shapes = IconButtonDefaults.shapes(),
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_edit),
            contentDescription = null,
        )
    }

    if (isEditDialogOpen) EditCommandDialog(id = id, onDismiss = { isEditDialogOpen = false })
}

@Composable
private fun CopyButton(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    id: Int,
    viewModel: CommandExamplesViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    IconButton(
        onClick = {
            weakHaptic()
            coroutineScope.launch {
                val command = viewModel.getCommandById(id) ?: ""
                if (command.isNotEmpty()) {
                    ClipboardUtils.copyToClipboard(text = command, context = context)
                    showToast(context, context.getString(R.string.copied_to_clipboard))
                } else {
                    showToast(context, context.getString(R.string.command_not_found))
                }
            }
        },
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shapes = IconButtonDefaults.shapes(),
        interactionSource = interactionSource,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_copy),
            contentDescription = null,
        )
    }

}

@Composable
private fun UseCommandButton(
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
        shapes = ButtonDefaults.shapes(),
        contentPadding = ButtonDefaults.contentPaddingFor(size)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_open_in_new),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(size))
        )

        Spacer(Modifier.widthIn(ButtonDefaults.iconSpacingFor(size)))

        AutoResizeableText(text = stringResource(R.string.use))
    }
}

@Composable
private fun FavouriteButton(
    id: Int,
    isFavourite: Boolean,
    viewModel: CommandExamplesViewModel
) {
    FavouriteIconButton(
        isFavorite = isFavourite,
        onToggle = {
            viewModel.toggleFavourite(
                id = id,
                isFavourite = !isFavourite,
                onSuccess = {}
            )
        })
}