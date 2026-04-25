@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.button.BackButton
import `in`.hridayan.ashell.core.presentation.components.floaters.FloatingIconsBackground
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.settings.presentation.components.item.PreferenceItemView
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.model.PreferenceGroup
import `in`.hridayan.ashell.settings.presentation.provider.getAllSettingsIcons
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val settings = viewModel.settingsPageList

    val floatingIconsResIds = getAllSettingsIcons()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.Navigate -> {
                    navController.navigate(event.route)
                }

                else -> {}
            }
        }
    }

    val listState = rememberLazyListState()
    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(topBar = {
        TopAppBar(
            title = {},
            navigationIcon = { BackButton() },
            scrollBehavior = scrollBehavior,
        )
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            state = listState,
            contentPadding = innerPadding
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(300.dp)
                ) {
                    FloatingIconsBackground(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(10.dp),
                        iconCount = 40,
                        iconResIds = floatingIconsResIds
                    )

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            SpinningGears(
                                modifier = Modifier.size(175.dp)
                            )
                        }

                        AutoResizeableText(
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .align(Alignment.CenterHorizontally),
                            text = stringResource(R.string.settings),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.displayLargeEmphasized.copy(
                                letterSpacing = 0.025.em,
                            )
                        )

                        AutoResizeableText(
                            modifier = Modifier
                                .padding(top = 10.dp, bottom = 25.dp)
                                .align(Alignment.CenterHorizontally),
                            text = stringResource(R.string.tweak_your_experience),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelLargeEmphasized
                        )
                    }
                }
            }

            itemsIndexed(settings) { index, group ->
                when (group) {
                    is PreferenceGroup.Category -> {
                        Text(
                            text = stringResource(group.categoryNameResId),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .animateItem()
                                .padding(horizontal = 20.dp, vertical = 25.dp)
                        )

                        group.items.forEach { item ->
                            PreferenceItemView(
                                item = item,
                                modifier = modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp, vertical = 1.dp)
                                    .animateItem()
                            )
                        }
                    }

                    is PreferenceGroup.Items -> {
                        val visibleItems = group.items.filter { it.isLayoutVisible }

                        visibleItems.forEachIndexed { i, item ->
                            val shape = getRoundedShape(i, visibleItems.size)

                            PreferenceItemView(
                                item = item,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp, vertical = 1.dp)
                                    .animateItem(),
                                shape = shape
                            )
                        }
                    }

                    else -> {}
                }
            }

            item {
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
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "bigGear"
    )

    val mediumGearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mediumGear"
    )

    val smallGearRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "smallGear"
    )

    BoxWithConstraints(
        modifier = modifier.aspectRatio(0.9f)
    ) {
        val base = minOf(maxWidth, maxHeight)

        Icon(
            modifier = Modifier
                .size(base * 0.7f)
                .align(Alignment.BottomStart)
                .graphicsLayer {
                    rotationZ = bigGearRotation
                },
            tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f),
            painter = painterResource(R.drawable.ic_settings_filled),
            contentDescription = null
        )

        Icon(
            modifier = Modifier
                .size(base * 0.35f)
                .align(Alignment.TopEnd)
                .offset(
                    x = -base * 0.1f,
                    y = base * 0.1f
                )
                .graphicsLayer {
                    rotationZ = mediumGearRotation
                },
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
            painter = painterResource(R.drawable.ic_settings_filled),
            contentDescription = null
        )

        Icon(
            modifier = Modifier
                .size(base * 0.18f)
                .align(Alignment.CenterEnd)
                .offset(y = base * 0.03f)
                .graphicsLayer {
                    rotationZ = smallGearRotation
                },
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
            painter = painterResource(R.drawable.ic_settings_filled),
            contentDescription = null
        )
    }
}