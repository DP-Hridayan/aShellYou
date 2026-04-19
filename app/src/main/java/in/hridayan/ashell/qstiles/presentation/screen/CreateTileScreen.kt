@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.qstiles.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
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
import `in`.hridayan.ashell.settings.presentation.components.switch.SettingsSwitch
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

    val executionMethodOptions = ButtonGroupOptionsProvider.tileServiceAdbExecutionMethod
    var showIconChooserDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteTileConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val interactionSource = remember { MutableInteractionSource() }

    val isValid = uiState.run {
        nameField.text.isNotBlank() && activeCommand.text.isNotBlank() &&
                (!isToggleable || inactiveCommand.text.isNotBlank()) && nameError == null
    }

    val floatingToolbarContainerColor =
        FloatingToolbarDefaults.vibrantFloatingToolbarColors().toolbarContainerColor
    val floatingToolbarContentColor =
        FloatingToolbarDefaults.vibrantFloatingToolbarColors().toolbarContentColor

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = if (uiState.isUpdateMode) stringResource(R.string.edit_tile)
        else stringResource(R.string.create_new_tile),
        fabContent = { _ ->
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
                                    enabled = isValid,
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
                                text = stringResource(R.string.update),
                                color = floatingToolbarContentColor.copy(alpha = if (isValid) 1f else 0.38f)
                            )
                            Icon(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.ic_help),
                                contentDescription = null,
                                tint = floatingToolbarContentColor.copy(alpha = if (isValid) 1f else 0.38f)
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
                    SectionLabel(
                        text = stringResource(R.string.tile_name),
                        modifier = Modifier.padding(top = 25.dp, start = 25.dp, bottom = 10.dp)
                    )
                }

                item {
                    TileTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        value = uiState.nameField,
                        onValueChange = { createTileViewModel.onNameChange(it) },
                        hint = "Reboot",
                        shape = RoundedCornerShape(50),
                        singleLine = true,
                        errorMessage = uiState.nameError
                    )
                }

                item {
                    SectionLabel(
                        text = stringResource(R.string.tile_behavior),
                        modifier = Modifier.padding(top = 25.dp, start = 25.dp, bottom = 10.dp)
                    )
                }

                item {
                    BehaviorSwitchRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        title = stringResource(R.string.toggleable),
                        description = stringResource(R.string.des_toggleable),
                        checked = uiState.isToggleable,
                        onCheckedChange = {
                            createTileViewModel.onToggleableChange(it)
                            weakHaptic()
                        },
                        roundedCornerShape = RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomStart = 4.dp,
                            bottomEnd = 4.dp
                        )
                    )
                }

                item {
                    BehaviorSwitchRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 2.dp),
                        title = stringResource(R.string.initial_state),
                        description = stringResource(R.string.des_initial_state),
                        checked = uiState.isActive,
                        onCheckedChange = {
                            createTileViewModel.onActiveStateChange(it)
                            weakHaptic()
                        },
                        checkedLabel = stringResource(R.string.on_state),
                        uncheckedLabel = stringResource(R.string.off_state),
                        roundedCornerShape = RoundedCornerShape(
                            topStart = 4.dp,
                            topEnd = 4.dp,
                            bottomStart = 24.dp,
                            bottomEnd = 24.dp
                        )
                    )
                }

                item {
                    val label = if (uiState.isToggleable)
                        stringResource(R.string.adb_command_on_state)
                    else
                        stringResource(R.string.adb_command)
                    SectionLabel(
                        text = label,
                        modifier = Modifier.padding(top = 25.dp, start = 25.dp, bottom = 10.dp)
                    )
                }

                item {
                    TileTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .animateContentSize(),
                        value = uiState.activeCommand,
                        onValueChange = { createTileViewModel.onActiveCommandChange(it) },
                        hint = stringResource(R.string.adb_command_hint),
                        shape = RoundedCornerShape(28.dp),
                        singleLine = false,
                        minLines = 3,
                        fontFamily = FontFamily.Monospace,
                    )
                }

                item {
                    AnimatedVisibility(
                        visible = uiState.isToggleable,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column {
                            SectionLabel(
                                text = stringResource(R.string.adb_command_off_state),
                                modifier = Modifier.padding(
                                    top = 20.dp, start = 25.dp, bottom = 10.dp
                                )
                            )
                            TileTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .animateContentSize(),
                                value = uiState.inactiveCommand,
                                onValueChange = { createTileViewModel.onInactiveCommandChange(it) },
                                hint = stringResource(R.string.adb_command_hint),
                                shape = RoundedCornerShape(28.dp),
                                singleLine = false,
                                minLines = 3,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
                    }
                }

                item {
                    val label = if (uiState.isToggleable)
                        stringResource(R.string.subtitle_on_state)
                    else
                        stringResource(R.string.subtitle)
                    SectionLabel(
                        text = label,
                        modifier = Modifier.padding(top = 25.dp, start = 25.dp, bottom = 10.dp)
                    )
                }
                item {
                    TileTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        value = uiState.run { if (isActive) activeSubtitle else inactiveSubtitle },
                        onValueChange = { createTileViewModel.onActiveSubtitleChange(it) },
                        hint = stringResource(R.string.on_state),
                        shape = RoundedCornerShape(50),
                        singleLine = true,
                    )
                }

                item {
                    AnimatedVisibility(
                        visible = uiState.isToggleable,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        Column {
                            SectionLabel(
                                text = stringResource(R.string.subtitle_off_state),
                                modifier = Modifier.padding(
                                    top = 20.dp, start = 25.dp, bottom = 10.dp
                                )
                            )
                            TileTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                value = uiState.inactiveSubtitle,
                                onValueChange = { createTileViewModel.onInactiveSubtitleChange(it) },
                                hint = stringResource(R.string.off_state),
                                shape = RoundedCornerShape(50),
                                singleLine = true,
                            )
                        }
                    }
                }

                item {
                    SectionLabel(
                        text = stringResource(R.string.execution_method),
                        modifier = Modifier.padding(top = 25.dp, start = 25.dp, bottom = 10.dp)
                    )
                }
                item {
                    Row(
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                    ) {
                        executionMethodOptions.forEachIndexed { index, option ->
                            ToggleButton(
                                checked = option.value == uiState.executionMode,
                                onCheckedChange = {
                                    createTileViewModel.onExecutionModeChange(option.value)
                                    weakHaptic()
                                },
                                modifier = Modifier.weight(1f),
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    executionMethodOptions.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                }
                            ) {
                                option.labelResId?.let { Text(stringResource(it)) }
                            }
                        }
                    }
                }

                item {
                    AnimatedVisibility(uiState.suggestedIcons.isNotEmpty()) {
                        SectionLabel(
                            text = stringResource(R.string.suggested_icons),
                            modifier = Modifier.padding(start = 25.dp, end = 25.dp, bottom = 10.dp)
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
                        uiState.suggestedIcons.forEach { iconKey ->
                            val tileIcon = TileIconProvider.iconById[iconKey]
                            val iconResId = tileIcon?.resId
                            val isIconSelected = tileIcon?.id == uiState.selectedIconId
                            iconResId?.let {
                                Box(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.run { if (isIconSelected) primary else surfaceVariant }
                                        )
                                        .clickable(onClick = withHaptic {
                                            createTileViewModel.onIconSelected(tileIcon.id)
                                        }),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(it),
                                        tint = if (isIconSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface,
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
                            .heightIn(min = 140.dp)
                            .padding(horizontal = 20.dp),
                        onClick = withHaptic { showIconChooserDialog = true })
                }

                item {
                    SectionLabel(
                        text = stringResource(R.string.preview),
                        modifier = Modifier.padding(top = 25.dp, start = 25.dp, bottom = 10.dp)
                    )
                }
                item {
                    val selectedIcon = TileIconProvider.iconById[uiState.selectedIconId]
                    val icon: Painter = if (selectedIcon?.resId != null)
                        painterResource(selectedIcon.resId)
                    else
                        painterResource(R.drawable.ic_add)

                    val previewSubtitle = uiState.run {
                        if (isToggleable) "${activeSubtitle.text} / ${inactiveSubtitle.text}"
                        else activeSubtitle.text
                    }

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
                                .height(120.dp)
                                .animateContentSize(),
                            icon = icon,
                            isActive = uiState.isActive
                        )
                        ModernTilePreview(
                            modifier = Modifier
                                .weight(2f)
                                .height(120.dp)
                                .widthIn(min = 120.dp)
                                .animateContentSize(),
                            icon = icon,
                            title = uiState.nameField.text,
                            subtitle = previewSubtitle,
                            isActive = uiState.isActive,
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
                            enabled = isValid,
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
            onQueryChange = { createTileViewModel.onIconQueryChange(it) },
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

/** Consistent section header label. */
@Composable
private fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    AutoResizeableText(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

/** Reusable text field card used throughout the screen. */
@Composable
private fun TileTextField(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    hint: String,
    shape: RoundedCornerShape = RoundedCornerShape(28.dp),
    singleLine: Boolean = true,
    minLines: Int = 1,
    fontFamily: FontFamily? = null,
    errorMessage: String? = null,
) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
            shape = shape,
        ) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                value = value,
                onValueChange = { onValueChange(it) },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontFamily = fontFamily,
                ),
                singleLine = singleLine,
                minLines = if (singleLine) 1 else minLines,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = if (singleLine) ImeAction.Done else ImeAction.Default,
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.text.isEmpty()) {
                            Text(
                                text = hint,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                                fontFamily = fontFamily,
                            )
                        }
                        innerTextField()
                    }
                },
            )
        }
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

/**
 * A labeled switch row used for the "Toggleable" and "Initial State" options.
 * Optionally shows a small badge text for the current checked state.
 */
@Composable
private fun BehaviorSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    checkedLabel: String? = null,
    uncheckedLabel: String? = null,
    roundedCornerShape: RoundedCornerShape = RoundedCornerShape(24.dp)
) {
    Card(
        modifier = modifier,
        shape = roundedCornerShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onCheckedChange(!checked) },
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    // State badge
                    if (checkedLabel != null && uncheckedLabel != null) {
                        val badge = if (checked) checkedLabel else uncheckedLabel
                        val badgeColor = if (checked)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.outline
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(badgeColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badge,
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeColor,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
            SettingsSwitch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun ClassicTile(
    modifier: Modifier = Modifier,
    icon: Painter,
    isActive: Boolean
) {
    val darkMode = LocalDarkMode.current

    val containerColor = MaterialTheme.colorScheme.run {
        if (isActive) primaryContainer else if (darkMode) surfaceContainerHigh else surfaceContainerLow
    }
    val contentColor = MaterialTheme.colorScheme.run {
        if (isActive) onPrimaryContainer else onSurface
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(20.dp)
                .size(64.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = CircleShape
        ) {}

        Box(
            modifier = modifier.fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(28.dp),
                painter = icon,
                tint = contentColor,
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ModernTilePreview(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    subtitle: String = "",
    isActive: Boolean = false,
    onClick: () -> Unit = {}
) {
    val darkMode = LocalDarkMode.current

    val containerColor = MaterialTheme.colorScheme.run {
        if (isActive) primaryContainer else if (darkMode) surfaceContainerHigh else surfaceContainerLow
    }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier.clickable(
            enabled = true,
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    modifier = Modifier.size(28.dp),
                    painter = icon,
                    tint = MaterialTheme.colorScheme.run { if (isActive) onPrimaryContainer else primary },
                    contentDescription = null
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.basicMarquee(),
                        text = title.ifEmpty { stringResource(R.string.untitled) },
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.run { if (isActive) onPrimaryContainer else onSurface },
                        maxLines = 1
                    )
                    if (subtitle.isNotEmpty()) {
                        Text(
                            modifier = Modifier.basicMarquee(),
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.run {
                                if (isActive) onPrimaryContainer.copy(alpha = 0.7f)
                                else onSurface.copy(alpha = 0.5f)
                            },
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChooseIconHintBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    onClick: () -> Unit = {},
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
            .clickable(enabled = true, onClick = onClick),
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