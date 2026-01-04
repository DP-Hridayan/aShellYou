@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.page.about.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.shape.CardCornerShape.getRoundedShape
import `in`.hridayan.ashell.core.presentation.components.shape.SineWaveShape
import `in`.hridayan.ashell.core.presentation.components.shape.WaveEdge
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.utils.syncedRotationAndScale
import `in`.hridayan.ashell.core.presentation.viewmodel.GithubDataViewModel
import `in`.hridayan.ashell.core.utils.openUrl
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.settings.presentation.components.card.SupportMeCard
import `in`.hridayan.ashell.settings.presentation.components.image.ProfilePic
import `in`.hridayan.ashell.settings.presentation.components.item.PreferenceItemView
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.event.SettingsUiEvent
import `in`.hridayan.ashell.settings.presentation.model.PreferenceGroup
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    githubDataViewModel: GithubDataViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
    val settings = settingsViewModel.aboutPageList
    val githubRepoStats by githubDataViewModel.stats.collectAsStateWithLifecycle()

    val (angle, scale) = syncedRotationAndScale()

    LaunchedEffect(Unit) {
        settingsViewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.Navigate -> {
                    navController.navigate(event.route)
                }

                is SettingsUiEvent.OpenUrl -> {
                    openUrl(event.url, context)
                }

                else -> {}
            }
        }
    }

    val listState = rememberLazyListState()

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.about),
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                state = listState,
                contentPadding = innerPadding
            ) {
                githubRepoStats?.totalDownloadCount?.let {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(15.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Spacer(
                                    modifier = Modifier
                                        .requiredSize(120.dp)
                                        .graphicsLayer {
                                            rotationZ = angle
                                        }
                                        .scale(scale)
                                        .clip(MaterialShapes.Cookie9Sided.toShape())
                                        .clickable(onClick = withHaptic {})
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                )
                                Icon(
                                    painter = painterResource(R.drawable.ic_adb2),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(60.dp)
                                )
                            }

                            AutoResizeableText(
                                text = stringResource(R.string.app_name),
                                fontWeight = FontWeight.Black,
                                fontStyle = FontStyle.Italic,
                                style = MaterialTheme.typography.displayLargeEmphasized.copy(
                                    letterSpacing = 0.025.em,
                                )
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(15.dp)
                            ) {
                                githubRepoStats?.stars?.let {
                                    GithubStatsChip(
                                        icon = painterResource(R.drawable.ic_star),
                                        statsText = it.toString(),
                                        statsDescription = stringResource(R.string.stargazers)
                                    )
                                }

                                githubRepoStats?.totalDownloadCount?.let {
                                    GithubStatsChip(
                                        icon = painterResource(R.drawable.ic_download),
                                        statsText = it.toCompactFormat(),
                                        statsDescription = stringResource(R.string.downloads),
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(15.dp)
                            ) {
                                githubRepoStats?.openIssues?.let {
                                    GithubStatsChip(
                                        icon = painterResource(R.drawable.ic_bug),
                                        statsText = it.toString(),
                                        statsDescription = stringResource(R.string.open_issues),
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }

                                githubRepoStats?.forks?.let {
                                    GithubStatsChip(
                                        icon = painterResource(R.drawable.ic_fork),
                                        statsText = it.toString(),
                                        statsDescription = stringResource(R.string.forks),
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer

                                    )
                                }
                            }

                            githubRepoStats?.license?.let {
                                GithubStatsChip(
                                    icon = painterResource(R.drawable.ic_license),
                                    statsText = it,
                                    statsDescription = stringResource(R.string.license),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    onClick = {
                                        openUrl(
                                            url = UrlConst.URL_GITHUB_REPO_LICENSE,
                                            context = context
                                        )
                                    }
                                )
                            }

                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (githubRepoStats?.totalDownloadCount != null) {
                                    Modifier
                                        .clip(
                                            SineWaveShape(
                                                amplitude = 10f,
                                                frequency = 5f,
                                                edge = WaveEdge.Both
                                            )
                                        )
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                } else Modifier
                            )
                            .animateItem(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.lead_developer),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 25.dp)
                                .align(Alignment.Start)
                                .animateItem()
                        )

                        ProfilePic(
                            painter = painterResource(R.mipmap.dp_hridayan),
                            size = 150.dp,
                        )

                        Text(
                            text = "Hridayan",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = stringResource(R.string.des_hridayan),
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                        )

                        SupportMeCard(
                            modifier = modifier.padding(
                                start = 15.dp,
                                end = 15.dp,
                                bottom = 25.dp
                            )
                        )
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
                                    .padding(
                                        start = 20.dp,
                                        end = 20.dp,
                                        top = 25.dp,
                                        bottom = 10.dp
                                    )
                            )
                            val visibleItems = group.items.filter { it.isLayoutVisible }

                            visibleItems.forEachIndexed { i, item ->
                                val shape = getRoundedShape(i, visibleItems.size)

                                PreferenceItemView(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    roundedShape = shape
                                )
                            }
                        }

                        is PreferenceGroup.Items -> {
                            val visibleItems = group.items.filter { it.isLayoutVisible }

                            visibleItems.forEachIndexed { i, item ->
                                val shape = getRoundedShape(i, visibleItems.size)

                                PreferenceItemView(
                                    item = item,
                                    modifier = Modifier.animateItem(),
                                    roundedShape = shape
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
        })
}

@Composable
private fun GithubStatsChip(
    modifier: Modifier = Modifier,
    icon: Painter,
    statsText: String,
    statsDescription: String,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    onClick: () -> Unit = {}
) {
    FilledTonalButton(
        modifier = modifier,
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor, contentColor),
        onClick = withHaptic(HapticFeedbackType.VirtualKey) { onClick() }
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = contentColor
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            AutoResizeableText(
                text = statsText,
                style = MaterialTheme.typography.titleMediumEmphasized,
            )
            AutoResizeableText(
                text = statsDescription,
                style = MaterialTheme.typography.bodySmallEmphasized,
                color = contentColor.copy(alpha = 0.85f)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
private fun Long.toCompactFormat(): String {
    return when {
        this < 1_000 -> this.toString()
        this < 1_000_000 -> String.format("%.1fK", this / 1_000f)
        this < 1_000_000_000 -> String.format("%.1fM", this / 1_000_000f)
        else -> String.format("%.1fB", this / 1_000_000_000f)
    }
        .replace(".0K", "K")
        .replace(".0M", "M")
        .replace(".0B", "B")
}