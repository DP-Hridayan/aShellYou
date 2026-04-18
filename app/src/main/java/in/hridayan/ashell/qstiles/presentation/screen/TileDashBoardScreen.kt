@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)

package `in`.hridayan.ashell.qstiles.presentation.screen

import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalDarkMode
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.common.constants.SHIZUKU_PACKAGE_NAME
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.presentation.components.card.IconWithTextCard
import `in`.hridayan.ashell.core.presentation.components.dashedBorder
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.utils.DateTimeUtils
import `in`.hridayan.ashell.core.utils.UrlUtils
import `in`.hridayan.ashell.core.utils.createAppNotificationSettingsIntent
import `in`.hridayan.ashell.core.utils.isAppInstalled
import `in`.hridayan.ashell.core.utils.isNotificationPermissionGranted
import `in`.hridayan.ashell.core.utils.launchApp
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.navigation.slideFadeInFromLeft
import `in`.hridayan.ashell.navigation.slideFadeInFromRight
import `in`.hridayan.ashell.navigation.slideFadeOutToLeft
import `in`.hridayan.ashell.navigation.slideFadeOutToRight
import `in`.hridayan.ashell.qstiles.data.provider.TileIconProvider
import `in`.hridayan.ashell.qstiles.domain.model.TileExecutionMode
import `in`.hridayan.ashell.qstiles.domain.model.TileLog
import `in`.hridayan.ashell.qstiles.presentation.model.TileDashBoardScreenUiState
import `in`.hridayan.ashell.qstiles.presentation.viewmodel.TileDashboardViewModel
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import rikka.shizuku.Shizuku
import java.util.Locale

data object TileScreenTabs {
    const val TILES: Int = 0
    const val LOGS: Int = 1
}

@Composable
fun TileDashBoardScreen(
    modifier: Modifier = Modifier,
    tileDashboardViewModel: TileDashboardViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val navController = LocalNavController.current
    val uiState by tileDashboardViewModel.state.collectAsState()
    val listState = rememberLazyListState()

    var hasNotificationAccess by remember { mutableStateOf(isNotificationPermissionGranted(context)) }
    val notificationSettingsIntent = createAppNotificationSettingsIntent(context)
    val onClickNotificationButton: () -> Unit = withHaptic {
        context.startActivity(notificationSettingsIntent)
    }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    hasNotificationAccess = isNotificationPermissionGranted(context)
                }
            }
        )
    }

    val title =
        if (uiState.currentTab == TileScreenTabs.TILES) stringResource(R.string.qs_tiles)
        else stringResource(R.string.tile_logs)

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = title,
        content = { innerPadding, topBarScrollBehavior ->

            Box(modifier = Modifier.fillMaxSize()) {
                val tilesListState = rememberLazyListState()
                val logsListState = rememberLazyListState()

                AnimatedContent(
                    targetState = uiState.currentTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideFadeInFromRight() togetherWith slideFadeOutToLeft()
                        } else {
                            slideFadeInFromLeft() togetherWith slideFadeOutToRight()
                        }
                    },
                    label = "tile_tabs"
                ) { tab ->

                    when (tab) {
                        TileScreenTabs.TILES -> {
                            TilesContent(
                                listState = tilesListState,
                                innerPadding = innerPadding,
                                topBarScrollBehavior = topBarScrollBehavior,
                                uiState = uiState,
                                hasNotificationAccess = hasNotificationAccess,
                                onClickNotificationButton = onClickNotificationButton,
                                navController = navController
                            )
                        }

                        TileScreenTabs.LOGS -> {
                            LogsContent(
                                listState = logsListState,
                                innerPadding = innerPadding,
                                topBarScrollBehavior = topBarScrollBehavior,
                                uiState = uiState,
                                viewModel = tileDashboardViewModel
                            )
                        }
                    }
                }

                if (uiState.logs.isEmpty() && uiState.currentTab == TileScreenTabs.LOGS) {
                    NoLogsUi(modifier = Modifier.align(Alignment.Center))
                }

                FloatingNavPill(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(50.dp),
                    selectedIndex = uiState.currentTab,
                    onSelectionChange = { tileDashboardViewModel.onTabChange(it) }
                )
            }
        })
}

@Composable
private fun TilesContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    topBarScrollBehavior: TopAppBarScrollBehavior,
    uiState: TileDashBoardScreenUiState,
    hasNotificationAccess: Boolean,
    onClickNotificationButton: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    var isShizukuInstalled by rememberSaveable {
        mutableStateOf(
            context.isAppInstalled(
                SHIZUKU_PACKAGE_NAME
            )
        )
    }

    val showShizukuUnavailableCard =
        !Shizuku.pingBinder() && uiState.tiles.any { it.executionMode == TileExecutionMode.SHIZUKU }

    val shizukuPermissionGranted = remember {
        mutableStateOf(
            if (Shizuku.pingBinder()) {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            } else false
        )
    }

    DisposableEffect(Unit) {
        val listener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            shizukuPermissionGranted.value =
                grantResult == PackageManager.PERMISSION_GRANTED
        }

        Shizuku.addRequestPermissionResultListener(listener)

        onDispose {
            Shizuku.removeRequestPermissionResultListener(listener)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .animateContentSize(),
        state = listState,
        contentPadding = innerPadding
    ) {

        item { Spacer(Modifier.height(25.dp)) }

        if (!hasNotificationAccess) {
            item {
                NotificationAccessRequestCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                    onClickButton = onClickNotificationButton
                )
            }
        }

        if (showShizukuUnavailableCard) {
            item {
                ShizukuUnavailableCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                    onClickButton = withHaptic {
                        if (isShizukuInstalled) context.launchApp(SHIZUKU_PACKAGE_NAME)
                        else UrlUtils.openUrl(
                            url = UrlConst.URL_SHIZUKU_SITE,
                            context = context
                        )
                    }
                )
            }
        }

        if (Shizuku.pingBinder()) {
            if (!shizukuPermissionGranted.value) {
                item {
                    ShizukuPermissionRequestCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, bottom = 20.dp),
                        onClickButton = withHaptic {
                            Shizuku.requestPermission(0)
                        }
                    )
                }
            }
        }

        item {
            val items = (1..10).toList()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                    onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                        navController.navigate(
                                            NavRoutes.CreateTileScreen(tileId = id)
                                        )
                                    }
                                )
                            } else {
                                val tileIcon = TileIconProvider.iconById[tileConfig.iconId]

                                ModernTile(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(80.dp),
                                    icon = painterResource(
                                        tileIcon?.resId ?: R.drawable.ic_adb
                                    ),
                                    title = tileConfig.name,
                                    subtitle = tileConfig.activeState.currentSubtitle,
                                    isActive = tileConfig.activeState.isActive,
                                    onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                                        navController.navigate(
                                            NavRoutes.CreateTileScreen(tileId = id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(140.dp)) }
    }
}

@Composable
private fun LogsContent(
    listState: LazyListState,
    innerPadding: PaddingValues,
    topBarScrollBehavior: TopAppBarScrollBehavior,
    uiState: TileDashBoardScreenUiState,
    viewModel: TileDashboardViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
        state = listState,
        contentPadding = innerPadding
    ) {

        item { Spacer(Modifier.height(25.dp)) }

        if (uiState.logs.isNotEmpty()) {

            item {
                LogStatsRow(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    totalExecutions = uiState.totalExecutions,
                    successRate = uiState.successRate
                )
            }

            item {
                RecentActivityHeader(
                    modifier = Modifier.padding(20.dp),
                    searchQuery = uiState.logsSearchQuery,
                    onSearchQueryChange = viewModel::onLogsSearchQueryChange
                )
            }

            items(uiState.logs, key = { it.id }) { log ->
                val tile = uiState.tiles.find { it.id == log.tileId }

                TileLogCard(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                    log = log,
                    tileName = tile?.name ?: "Deleted Tile",
                    iconId = tile?.iconId ?: "terminal"
                )
            }
        }

        item { Spacer(Modifier.height(140.dp)) }
    }
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
            .clickable(enabled = true, onClick = onClick),
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
            style = MaterialTheme.typography.titleMedium,
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
    subtitle: String = "",
    isActive: Boolean = true,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        ),
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
                tint = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
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
                    maxLines = 1
                )

                if (subtitle.isNotEmpty()) {
                    Text(
                        modifier = Modifier.basicMarquee(),
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isActive)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun LogStatsRow(
    modifier: Modifier = Modifier,
    totalExecutions: Int,
    successRate: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        StatCard(
            modifier = Modifier.weight(1.2f),
            label = stringResource(R.string.total_executions).uppercase(Locale.getDefault()),
            value = totalExecutions.toString(),
            icon = painterResource(R.drawable.ic_analytics_filled),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.success_rate).uppercase(Locale.getDefault()),
            value = successRate,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: Painter? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AutoResizeableText(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = contentColor.copy(alpha = 0.7f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AutoResizeableText(
                    text = value,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black
                )
                if (icon != null) {
                    Icon(
                        modifier = Modifier.size(64.dp),
                        painter = icon,
                        contentDescription = null,
                        tint = contentColor.copy(alpha = 0.15f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivityHeader(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val weakHaptic = LocalWeakHaptic.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Crossfade(targetState = isSearchExpanded) { expanded ->
            if (expanded) {
                BasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            AutoResizeableText(
                                text = stringResource(R.string.search_tile),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            } else {
                Text(
                    text = stringResource(R.string.recent_activity),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    isSearchExpanded = !isSearchExpanded
                    weakHaptic()
                }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(if (isSearchExpanded) R.drawable.ts_close else R.drawable.ic_filter_alt),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            if (!isSearchExpanded) {
                Text(
                    text = stringResource(R.string.filter),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TileLogCard(
    modifier: Modifier = Modifier,
    log: TileLog,
    tileName: String,
    iconId: String
) {
    val darkMode = LocalDarkMode.current
    val tileIcon = TileIconProvider.iconById[iconId]

    val badgeColor =
        if (log.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (darkMode) MaterialTheme.colorScheme.surfaceContainerLow else MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        painter = if (tileIcon != null) painterResource(tileIcon.resId) else painterResource(
                            R.drawable.ic_adb
                        ),
                        contentDescription = null,
                        tint = badgeColor
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val modeLabel =
                        if (log.executionMode == TileExecutionMode.SHIZUKU) stringResource(R.string.shizuku)
                        else stringResource(R.string.root)
                    Text(
                        text = "$modeLabel • ${DateTimeUtils.getRelativeTime(log.timestamp)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (log.isSuccess) stringResource(R.string.success)
                        else stringResource(R.string.failed),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(badgeColor.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 25.dp, vertical = 15.dp)
                            .basicMarquee(),
                        text = log.command,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 1
                    )
                }
            }

            if (!log.isSuccess && log.output.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    text = stringResource(R.string.error) + ": ${log.output}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun FloatingNavPill(
    modifier: Modifier = Modifier,
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit
) {
    val isDarkMode = LocalDarkMode.current
    val motion = MaterialTheme.motionScheme

    val navItems = listOf(stringResource(R.string.tiles), stringResource(R.string.logs))

    Card(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
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
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = withHaptic { onSelectionChange(index) }
                            ),
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
private fun ShizukuUnavailableCard(
    modifier: Modifier = Modifier,
    onClickButton: () -> Unit = {},
) {
    IconWithTextCard(
        modifier = modifier,
        icon = painterResource(R.drawable.ic_shizuku),
        text = stringResource(R.string.shizuku_unavailable_message),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        content = {
            Button(
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                onClick = { onClickButton() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_open_in_new),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(10.dp))
                AutoResizeableText(
                    text = stringResource(R.string.shizuku),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    )
}

@Composable
private fun ShizukuPermissionRequestCard(
    modifier: Modifier = Modifier,
    onClickButton: () -> Unit = {},
) {
    IconWithTextCard(
        modifier = modifier,
        icon = painterResource(R.drawable.ic_shizuku),
        text = stringResource(R.string.grant_shizuku_permission_for_tiles),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        content = {
            Button(
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                onClick = { onClickButton() }
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.grant_permission),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    )
}

@Composable
private fun NotificationAccessRequestCard(
    modifier: Modifier = Modifier,
    onClickButton: () -> Unit = {},
) {
    IconWithTextCard(
        modifier = modifier,
        icon = painterResource(R.drawable.ic_notification),
        text = stringResource(R.string.tile_dashboard_notification_access_message),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        content = {
            Button(
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ),
                onClick = { onClickButton() }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_open_in_new),
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(10.dp))
                AutoResizeableText(
                    text = stringResource(R.string.notification_settings),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    )
}

@Composable
fun NoLogsUi(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(MaterialShapes.Cookie9Sided.toShape())
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(80.dp),
                painter = painterResource(R.drawable.ic_sentiment_dissatisfied),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        }

        AutoResizeableText(
            text = stringResource(R.string.no_logs_yet),
            style = MaterialTheme.typography.titleLargeEmphasized,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 50.dp, bottom = 15.dp)
        )

        Text(
            text = stringResource(R.string.no_logs_yet_msg),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}
