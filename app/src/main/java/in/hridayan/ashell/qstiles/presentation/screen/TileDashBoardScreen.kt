@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.qstiles.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.qs_tiles),
        content = { innerPadding, topBarScrollBehavior ->

            Box(modifier = Modifier.fillMaxSize()) {
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
                        val items = (1..10).toList()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                                ) {
                                    rowItems.forEach { id ->
                                        val tileConfig = uiState.tiles.find { it.id == id }

                                        if (tileConfig == null) {
                                            EmptyTileBox(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(80.dp),
                                                tileId = id - 1,
                                                onClick = withHaptic {
                                                    navController.navigate(
                                                        NavRoutes.CreateTileScreen(
                                                            tileId = id
                                                        )
                                                    )
                                                }
                                            )
                                        } else {
                                            val tileIcon =
                                                TileIconProvider.iconById[tileConfig.iconId]
                                            ModernTile(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(80.dp),
                                                icon = if (tileIcon != null) painterResource(
                                                    tileIcon.resId
                                                ) else painterResource(
                                                    R.drawable.ic_adb
                                                ),
                                                title = tileConfig.name,
                                                isActive = tileConfig.isActive,
                                                onClick = withHaptic {
                                                    navController.navigate(
                                                        NavRoutes.CreateTileScreen(
                                                            tileId = id
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
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

                FloatingNavPill(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(50.dp)
                )
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
    Row(
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            modifier = Modifier
                .size(48.dp)
                .padding(start = 20.dp),
            painter = painterResource(R.drawable.ic_add),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            contentDescription = null
        )

        Text(
            modifier = Modifier
                .weight(1f)
                .basicMarquee()
                .padding(end = 20.dp),
            text = stringResource(R.string.tile) + " ${tileId + 1}",
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
            maxLines = 1
        )
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
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = icon,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )

                AutoResizeableText(
                    text = if (isActive) stringResource(R.string.on) else stringResource(R.string.off),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
            }
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