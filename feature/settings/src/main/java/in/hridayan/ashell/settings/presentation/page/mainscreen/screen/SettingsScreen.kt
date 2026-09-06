@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.page.mainscreen.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import `in`.hridayan.ashell.core.common.FeatureConfig
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.floaters.FloatingIconsBackground
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.provider.getAllSettingsIcons
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.settingsdsl.ui.SettingsColumn

private const val ITEM_KEY_HEADER = "header"
private const val FLOATING_ICONS_COUNT = 40
private val HEADER_MIN_HEIGHT = 300.dp
private val FLOATING_ICONS_PADDING = 10.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen() {
    val navController = LocalNavController.current
    val settings = LocalSettings.current
    val hapticsEnabled = settings[SettingsKeys.HapticsAndVibration]
    val floatingIconsResIds = getAllSettingsIcons()

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    BackButton {
                        navController.navigateBack()
                    }
                },
                actions = {
                    IconButton(onClick = withHaptic { navController.navigate(NavRoutes.SettingsSearchScreen) }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_search),
                            contentDescription = stringResource(R.string.search_settings),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->

        SettingsColumn(
            modifier = Modifier,
            contentPadding = paddingValues,
            topAppBarState = scrollBehavior.state,
            listState = listState,
            hapticsEnabled = hapticsEnabled,
        ) {
            item(ITEM_KEY_HEADER) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(HEADER_MIN_HEIGHT)
                ) {
                    FloatingIconsBackground(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(FLOATING_ICONS_PADDING),
                        iconCount = FLOATING_ICONS_COUNT,
                        iconResIds = floatingIconsResIds,
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            SpinningGears(modifier = Modifier.size(175.dp))
                        }

                        AutoResizeableText(
                            modifier = Modifier
                                .padding(top = 20.dp, start = 15.dp, end = 15.dp)
                                .align(Alignment.CenterHorizontally),
                            text = stringResource(R.string.settings),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.displayLargeEmphasized.copy(
                                letterSpacing = 0.025.em
                            ),
                            maxLines = 1,
                        )

                        AutoResizeableText(
                            modifier = Modifier
                                .padding(top = 10.dp, bottom = 25.dp)
                                .align(Alignment.CenterHorizontally),
                            text = stringResource(R.string.tweak_your_experience),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLargeEmphasized,
                        )
                    }
                }
            }

            group {
                clickableItem(SettingsKeys.LookAndFeel) {
                    title(R.string.look_and_feel)
                    description(R.string.des_look_and_feel)
                    icon(R.drawable.ic_pallete)
                    onClick { navController.navigate(NavRoutes.LookAndFeelScreen) }
                }

                clickableItem(SettingsKeys.Behavior) {
                    title(R.string.behavior)
                    description(R.string.des_behavior)
                    icon(R.drawable.ic_sentiment_neutral)
                    onClick { navController.navigate(NavRoutes.BehaviorScreen) }
                }

                clickableItem(SettingsKeys.QuickSettingsTiles) {
                    title(R.string.qs_tiles)
                    description(R.string.des_qs_tiles)
                    icon(R.drawable.ic_dashboard)
                    onClick { navController.navigate(NavRoutes.TileDashboardScreen) }
                }

                clickableItem(SettingsKeys.CloudModels) {
                    title(R.string.ai_models)
                    description(R.string.des_ai_models)
                    icon(Icons.Outlined.AutoAwesome)
                    visible { FeatureConfig.isAiEnabled }
                    onClick { navController.navigate(NavRoutes.AiModelsScreen) }
                }

                clickableItem(SettingsKeys.AutoUpdate) {
                    title(R.string.auto_update)
                    description(R.string.des_auto_update)
                    icon(R.drawable.ic_auto_update)
                    onClick { navController.navigate(NavRoutes.AutoUpdateScreen) }
                }

                clickableItem(SettingsKeys.BackupAndRestore) {
                    title(R.string.backup_and_restore)
                    description(R.string.des_backup_and_restore)
                    icon(R.drawable.ic_settings_backup_restore)
                    onClick { navController.navigate(NavRoutes.BackupAndRestoreScreen) }
                }

                clickableItem(SettingsKeys.About) {
                    title(R.string.about)
                    description(R.string.des_about)
                    icon(R.drawable.ic_info)
                    onClick { navController.navigate(NavRoutes.AboutScreen) }
                }
            }

            item(key = "spacer_bottom") {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                )
            }
        }
    }
}

@Composable
private fun SpinningGears(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "gears")

    val bigGearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bigGear",
    )
    val mediumGearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mediumGear",
    )
    val smallGearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smallGear",
    )

    BoxWithConstraints(modifier = modifier.aspectRatio(0.9f)) {
        val base = minOf(maxWidth, maxHeight)

        Icon(
            modifier = Modifier
                .size(base * 0.7f)
                .align(Alignment.BottomStart)
                .graphicsLayer { rotationZ = bigGearRotation },
            tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
            painter = painterResource(R.drawable.ic_settings_filled),
            contentDescription = null
        )
        Icon(
            modifier = Modifier
                .size(base * 0.35f)
                .align(Alignment.TopEnd)
                .offset(x = -base * 0.1f, y = base * 0.1f)
                .graphicsLayer { rotationZ = mediumGearRotation },
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
            painter = painterResource(R.drawable.ic_settings_filled),
            contentDescription = null
        )
        Icon(
            modifier = Modifier
                .size(base * 0.18f)
                .align(Alignment.CenterEnd)
                .offset(y = base * 0.03f)
                .graphicsLayer { rotationZ = smallGearRotation },
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            painter = painterResource(R.drawable.ic_settings_filled),
            contentDescription = null
        )
    }
}
