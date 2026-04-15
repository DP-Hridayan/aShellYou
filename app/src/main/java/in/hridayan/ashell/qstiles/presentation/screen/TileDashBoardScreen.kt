@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.qstiles.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.card.PillShapedCard
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

    val tileIcon = painterResource(R.drawable.ts_wifi_tethering)

    val onCreateNewTile: () -> Unit = withHaptic {
        navController.navigate(NavRoutes.CreateTileScreen)
    }

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.qs_tiles),
        fabContent = {
            FloatingActionButton(
                onClick = onCreateNewTile,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = null
                )
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
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(25.dp)
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp)
                    ) {
                        PillShapedCard(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.activeCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = if (uiState.activeCount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            AutoResizeableText(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelMedium,
                                text = "${uiState.activeCount} " + stringResource(R.string.active_tiles)
                            )
                        }
                    }
                }

                itemsIndexed(uiState.tiles) { _, config ->
                    val tileIcon = TileIconProvider.iconById[config.iconId]

                    TileDetailsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        iconResId = tileIcon?.resId,
                        tileName = config.name,
                        isActive = config.isActive,
                        toggleTileState = withHaptic {
                            tileDashboardViewModel.toggleTile(config)
                        }
                    )
                }
            }
        })
}

@Composable
private fun TileDetailsCard(
    modifier: Modifier = Modifier,
    iconResId: Int? = null,
    tileName: String = stringResource(R.string.tile_name),
    isActive: Boolean = true,
    toggleTileState: () -> Unit = {}
) {
    val tileStatus =
        if (isActive) stringResource(R.string.active) else stringResource(R.string.disabled)
    val tileContainerColor =
        if (isActive) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val tileContentColor =
        if (isActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface

    val onClickEdit: () -> Unit = {}

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
                    onClick = onClickEdit,
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

@Preview(
    showBackground = true
)
@Composable
private fun PreviewTileDetailsCard() {
    TileDetailsCard()
}

@Preview(showBackground = true)
@Composable
private fun PreviewQSTileDashboard() {
    TileDashBoardScreen()
}