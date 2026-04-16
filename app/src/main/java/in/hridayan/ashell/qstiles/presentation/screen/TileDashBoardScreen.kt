@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.qstiles.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.presentation.components.dashedBorder
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.presentation.viewmodel.TileDashboardViewModel
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold

@Composable
fun TileDashBoardScreen(
    modifier: Modifier = Modifier,
    tileDashboardViewModel: TileDashboardViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val uiState by tileDashboardViewModel.state.collectAsState()

    val onCreateNewTile: () -> Unit = withHaptic {
        navController.navigate(NavRoutes.CreateTileScreen)
    }

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.qs_tiles),
        fabContent = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 25.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                FloatingNavPill(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(start = 50.dp)
                )

                FloatingActionButton(
                    onClick = onCreateNewTile,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null
                    )
                }
            }

        },
        content = { innerPadding, topBarScrollBehavior ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
                    .animateContentSize(),
                state = listState,
                contentPadding = innerPadding
            ) {
                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }

                item {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        maxItemsInEachRow = 2
                    ) {
                        (1..10).forEach { id ->
                            val config = uiState.tiles.find { it.id == id }
                            if (config == null) {
                                EmptyTileBox(
                                    modifier = Modifier.weight(1f),
                                    tileId = id - 1, // EmptyTileBox expects 0-indexed for display
                                    onClick = {
                                        navController.navigate(NavRoutes.CreateTileScreen(tileId = id))
                                    }
                                )
                            } else {
                                val tileIcon = TileIconProvider.iconById[config.iconId]
                                ModernTile(
                                    modifier = Modifier.weight(1f),
                                    icon = if (tileIcon != null) painterResource(tileIcon.resId) else painterResource(R.drawable.ic_adb),
                                    title = config.name,
                                    isActive = config.isActive,
                                    onClick = {
                                        navController.navigate(NavRoutes.CreateTileScreen(tileId = id))
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            }
        })
}

@Composable
private fun EmptyTileBox(
    modifier: Modifier = Modifier,
    tileId: Int,
    cornerRadius: Dp = 24.dp,
    onClick: () -> Unit = {}
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
                enabled = true,
                onClick = onClick
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
            text = stringResource(R.string.tile) + " ${tileId + 1}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TileDetailsCard(
    modifier: Modifier = Modifier,
    iconResId: Int? = null,
    tileName: String = stringResource(R.string.tile_name),
    isActive: Boolean = true,
    onEditTile: () -> Unit = {},
    toggleTileState: () -> Unit = {}
) {
    val tileStatus =
        if (isActive) stringResource(R.string.active) else stringResource(R.string.disabled)
    val tileContainerColor =
        if (isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val tileContentColor =
        if (isActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface

    val toggleButtonText =
        if (isActive) stringResource(R.string.disable) else stringResource(R.string.enable)

    Card(
        modifier = modifier.clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = tileContainerColor,
            contentColor = tileContentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    iconResId?.let {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(it),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            contentDescription = null,
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tileName,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.SemiBold,
                        color = tileContentColor
                    )

                    AutoResizeableText(
                        text = tileStatus,
                        style = MaterialTheme.typography.labelLarge,
                        color = tileContentColor.copy(alpha = if (isActive) 1f else 0.7f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onEditTile,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = tileContainerColor,
                        contentColor = tileContentColor
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = tileContentColor.copy(0.5f)
                    ),
                    shapes = ButtonDefaults.shapes()
                ) {
                    AutoResizeableText(text = stringResource(R.string.edit_tile))
                }

                Button(
                    onClick = toggleTileState,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        contentColor = if (isActive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                    ),
                    shapes = ButtonDefaults.shapes()
                ) {
                    AutoResizeableText(text = toggleButtonText)
                }
            }
        }
    }
}

@Composable
private fun NoTilesUi(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    cornerRadius: Dp = 24.dp,
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
                enabled = true,
                onClick = onClick
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
            text = stringResource(R.string.create_new_tile_description),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActiveTilesCountPill(
    modifier: Modifier = Modifier,
    activeCount: Int
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier.align(Alignment.CenterEnd),
            shape = RoundedCornerShape(50),
            colors = CardDefaults.cardColors(
                containerColor = if (activeCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                contentColor = if (activeCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        ) {
            AutoResizeableText(
                modifier = Modifier.padding(
                    horizontal = 10.dp,
                    vertical = 3.dp
                ),
                style = MaterialTheme.typography.labelMedium,
                text = "$activeCount " + stringResource(R.string.active_tiles)
            )
        }
    }
}

@Composable
private fun FloatingNavPill(modifier: Modifier = Modifier) {
    val isDarkMode = LocalDarkMode.current
    val motion = MaterialTheme.motionScheme

    val navItems = listOf(
        stringResource(R.string.dashboard),
        stringResource(R.string.logs)
    )

    var selectedIndex by remember { mutableIntStateOf(0) }

    Card(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp,
        )
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val itemWidth = maxWidth / navItems.size

            val offsetX by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = motion.fastSpatialSpec(),
                label = "pill_offset"
            )

            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .padding(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                enabled = true,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = withHaptic {
                                    selectedIndex = index
                                }),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoResizeableText(
                            text = item,
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            fontWeight = FontWeight.SemiBold,
                            color = if (index == selectedIndex)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernTile(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    isActive: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .padding(20.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
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