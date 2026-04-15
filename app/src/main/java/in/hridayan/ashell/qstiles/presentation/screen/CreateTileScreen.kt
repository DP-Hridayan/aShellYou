@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.qstiles.presentation.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.presentation.viewmodel.CreateTileViewModel
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.provider.ButtonGroupOptionsProvider

@Composable
fun CreateTileScreen(
    modifier: Modifier = Modifier,
    createTileViewModel: CreateTileViewModel = hiltViewModel()
) {
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current

    val uiState by createTileViewModel.state.collectAsState()

    val tileNameHint = "Reboot"

    val adbCommandHint = "adb reboot"

    val executionMethodOptions = ButtonGroupOptionsProvider.tileServiceAdbExecutionMethod

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.create_new_tile),
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
                            top = 25.dp,
                            start = 25.dp,
                            bottom = 10.dp
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
                            }
                        )
                    }
                }

                item {
                    AutoResizeableText(
                        text = stringResource(R.string.adb_command),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            top = 25.dp,
                            start = 25.dp,
                            bottom = 10.dp
                        )
                    )
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .animateContentSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(28.dp)
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
                            }
                        )
                    }
                }

                item {
                    AutoResizeableText(
                        text = stringResource(R.string.execution_method),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            top = 25.dp,
                            start = 25.dp,
                            bottom = 10.dp
                        )
                    )
                }

                item {
                    Row(
                        modifier = modifier.padding(horizontal = 20.dp),
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
                                top = 25.dp,
                                start = 25.dp,
                                bottom = 10.dp
                            )
                        )
                    }
                }

                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        maxItemsInEachRow = 5
                    ) {
                        uiState.suggestedIcons.forEachIndexed { _, icon ->

                            val icon = TileIconProvider.iconById[icon]
                            val iconResId = icon?.resId

                            iconResId?.let {
                                Box(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable(
                                            enabled = true,
                                            onClick = withHaptic {
                                                createTileViewModel.onIconSelected(icon.id)
                                            }),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(it),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .padding(start = 25.dp, end = 25.dp, top = 15.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            .dashedBorder(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                strokeWidth = 2.dp,
                                cornerRadius = 24.dp
                            )
                            .clickable(
                                enabled = true,
                                onClick = withHaptic { }),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 25.dp)
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
                            modifier = Modifier.padding(bottom = 25.dp),
                            text = stringResource(R.string.des_choose_icon),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                item {
                    AutoResizeableText(
                        text = stringResource(R.string.preview),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            top = 25.dp,
                            start = 25.dp,
                            bottom = 10.dp
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
                                .padding(horizontal = 20.dp)
                                .animateContentSize(),
                            icon = icon,
                            title = uiState.name
                        )

                        ModernTile(
                            modifier = Modifier
                                .widthIn(min = 120.dp)
                                .padding(horizontal = 20.dp)
                                .animateContentSize(),
                            icon = icon,
                            title = uiState.name
                        )
                    }
                }

                item {
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
                        shapes = ButtonDefaults.shapes(),
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
        })
}

@Composable
private fun ClassicTile(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String
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
        AutoResizeableText(
            text = title.ifEmpty { stringResource(R.string.untitled) },
            style = MaterialTheme.typography.labelLargeEmphasized,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ModernTile(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    isActive: Boolean = true
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
                    AutoResizeableText(
                        text = title.ifEmpty { stringResource(R.string.untitled) },
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AutoResizeableText(
                        text = if (isActive) stringResource(R.string.on) else stringResource(R.string.off),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewScreen() {
    CompositionLocalProvider(
        LocalWeakHaptic provides {}
    ) {
        CreateTileScreen()
    }
}

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.dp,
    cornerRadius: Dp = 0.dp,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 6.dp
) = this.then(
    Modifier.drawBehind {
        val stroke = strokeWidth.toPx()
        val dash = dashLength.toPx()
        val gap = gapLength.toPx()

        drawRoundRect(
            color = color,
            size = size,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(dash, gap)
                )
            )
        )
    }
)