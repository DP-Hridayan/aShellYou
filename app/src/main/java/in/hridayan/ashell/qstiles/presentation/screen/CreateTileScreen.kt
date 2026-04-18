@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.qstiles.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.dashedBorder
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.showToast
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.presentation.components.dialog.DeleteTileConfirmationDialog
import `in`.hridayan.ashell.qstiles.presentation.components.dialog.IconChooserDialog
import `in`.hridayan.ashell.qstiles.presentation.viewmodel.CreateTileViewModel
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.provider.ButtonGroupOptionsProvider

@Composable
fun CreateTileScreen(
    modifier: Modifier = Modifier,
    tileId: Int,
    createTileViewModel: CreateTileViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    val res = LocalResources.current

    val uiState by createTileViewModel.state.collectAsState()
    val iconsList by createTileViewModel.iconsList.collectAsState()

    val tileNameHint = "Reboot"
    val adbCommandHint = "adb reboot"

    val executionMethodOptions = ButtonGroupOptionsProvider.tileServiceAdbExecutionMethod
    var showIconChooserDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteTileConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = if (uiState.isUpdateMode) stringResource(R.string.edit_tile) else stringResource(
            R.string.create_new_tile
        ),
        fabContent = { expanded ->
            if (uiState.isUpdateMode) {
                HorizontalFloatingToolbar(
                    expanded = true,
                    floatingActionButton = {
                        FloatingToolbarDefaults.VibrantFloatingActionButton(
                            onClick = withHaptic(HapticFeedbackType.Reject) {
                                showDeleteTileConfirmationDialog = true
                            },
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_delete),
                                contentDescription = "Delete Tile"
                            )
                        }
                    },
                    colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                    content = {
                        Row(
                            modifier = Modifier
                                .clickable(
                                    enabled = true,
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = withHaptic {
                                        createTileViewModel.createTile()
                                        navController.popBackStack()
                                    })
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AutoResizeableText(
                                text = stringResource(R.string.update)
                            )

                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.ic_help),
                                contentDescription = null
                            )
                        }
                    })
            }
        },
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding
            ) {
                item {
                    AutoResizeableText(
                        text = stringResource(R.string.tile_name),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            top = 25.dp, start = 25.dp, bottom = 10.dp
                        )
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        BasicTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 16.dp),
                            value = uiState.name,
                            onValueChange = { createTileViewModel.onNameChange(it) },
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Send
                            ),
                            decorationBox = { innerTextField ->

                                Box {
                                    if (uiState.name.isEmpty()) {
                                        Text(
                                            text = tileNameHint,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                    }

                                    innerTextField()
                                }
                            })
                    }
                }

                item {
                    AutoResizeableText(
                        text = stringResource(R.string.adb_command),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            top = 25.dp, start = 25.dp, bottom = 10.dp
                        )
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .animateContentSize(), colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ), shape = RoundedCornerShape(28.dp)
                    ) {
                        BasicTextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(all = 16.dp),
                            value = uiState.command,
                            onValueChange = { createTileViewModel.onCommandChange(it) },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSecondaryContainer

                            ),
                            minLines = 3,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            decorationBox = { innerTextField ->

                                Box {
                                    if (uiState.command.isEmpty()) {
                                        Text(
                                            text = adbCommandHint,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                alpha = 0.6f
                                            )
                                        )
                                    }

                                    innerTextField()
                                }
                            })
                    }
                }

                item {
                    AutoResizeableText(
                        text = stringResource(R.string.execution_method),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            top = 25.dp, start = 25.dp, bottom = 10.dp
                        )
                    )
                }

                item {
                    Row(
                        modifier = modifier.padding(start = 20.dp, end = 20.dp, bottom = 25.dp),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                    ) {
                        executionMethodOptions.forEachIndexed { index, option ->
                            ToggleButton(
                                checked = option.value == uiState.executionMode, onCheckedChange = {
                                    createTileViewModel.onExecutionModeChange(option.value)
                                    weakHaptic()
                                }, modifier = Modifier.weight(1f), shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    executionMethodOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                }
                            ) {
                                option.labelResId?.let {
                                    Text(stringResource(it))
                                }
                            }
                        }
                    }
                }

                item {
                    AnimatedVisibility(uiState.suggestedIcons.isNotEmpty()) {
                        AutoResizeableText(
                            text = stringResource(R.string.suggested_icons),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                start = 25.dp, end = 25.dp, bottom = 10.dp
                            )
                        )
                    }
                }

                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 15.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        maxItemsInEachRow = 5
                    ) {
                        uiState.suggestedIcons.forEachIndexed { _, icon ->

                            val icon = TileIconProvider.iconById[icon]
                            val iconResId = icon?.resId
                            val isIconSelected = icon?.id == uiState.selectedIconId

                            iconResId?.let {
                                Box(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (isIconSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable(
                                            enabled = true, onClick = withHaptic {
                                                createTileViewModel.onIconSelected(icon.id)
                                            }), contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(it),
                                        tint = if (isIconSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    ChooseIconHintBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp)
                            .padding(horizontal = 20.dp),
                        onClick = withHaptic { showIconChooserDialog = true })
                }

                item {
                    AutoResizeableText(
                        text = stringResource(R.string.preview),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            top = 25.dp, start = 25.dp, bottom = 10.dp
                        )
                    )
                }

                item {
                    val selectedIcon = TileIconProvider.iconById[uiState.selectedIconId]
                    val iconResId = selectedIcon?.resId
                    val icon =
                        if (iconResId != null) painterResource(iconResId) else painterResource(R.drawable.ic_add)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .dashedBorder(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                strokeWidth = 2.dp,
                                cornerRadius = 24.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ClassicTile(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 20.dp)
                                .animateContentSize(),
                            icon = icon,
                            title = uiState.name
                        )

                        ModernTile(
                            modifier = Modifier
                                .weight(2f)
                                .widthIn(min = 120.dp)
                                .animateContentSize(), icon = icon, title = uiState.name
                        )
                    }
                }

                item {
                    if (uiState.isUpdateMode) {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )
                    } else {
                        Button(
                            modifier = Modifier
                                .heightIn(ButtonDefaults.ExtraLargeContainerHeight)
                                .fillMaxWidth()
                                .padding(horizontal = 60.dp, vertical = 30.dp),
                            enabled = uiState.name.isNotEmpty() && uiState.command.isNotEmpty(),
                            onClick = withHaptic {
                                createTileViewModel.createTile()
                                navController.popBackStack()
                            },
                        ) {
                            AutoResizeableText(
                                text = stringResource(R.string.generate_tile),
                                style = MaterialTheme.typography.titleLargeEmphasized
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            Icon(
                                painter = painterResource(R.drawable.ic_help),
                                contentDescription = null,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        })

    if (showIconChooserDialog) {
        IconChooserDialog(
            onDismiss = { showIconChooserDialog = false },
            icons = iconsList,
            searchQuery = uiState.iconSearchQuery,
            onQueryChange = {
                createTileViewModel.onIconQueryChange(it)
            },
            onIconSelected = {
                createTileViewModel.onIconSelected(it)
                showIconChooserDialog = false
                showToast(context, res.getString(R.string.icon_selected))
            })
    }

    if (showDeleteTileConfirmationDialog) {
        DeleteTileConfirmationDialog(
            onDismiss = { showDeleteTileConfirmationDialog = false },
            onConfirm = {
                createTileViewModel.deleteTile()
                navController.popBackStack()
            })
    }
}

@Composable
private fun ClassicTile(
    modifier: Modifier = Modifier, icon: Painter, title: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                contentDescription = null
            )
        }
        Text(
            modifier = Modifier.basicMarquee(),
            text = title.ifEmpty { stringResource(R.string.untitled) },
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}

@Composable
private fun ModernTile(
    modifier: Modifier = Modifier, icon: Painter, title: String, isActive: Boolean = true
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier.padding(20.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(25.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = icon,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentDescription = null
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.basicMarquee(),
                        text = title.ifEmpty { stringResource(R.string.untitled) },
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier.basicMarquee(),
                        text = if (isActive) stringResource(R.string.on) else stringResource(R.string.off),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun ChooseIconHintBox(
    modifier: Modifier = Modifier, cornerRadius: Dp = 24.dp, onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .dashedBorder(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                strokeWidth = 2.dp,
                cornerRadius = cornerRadius
            )
            .clickable(
                enabled = true, onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null
            )
        }

        Text(
            text = stringResource(R.string.des_choose_icon),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}