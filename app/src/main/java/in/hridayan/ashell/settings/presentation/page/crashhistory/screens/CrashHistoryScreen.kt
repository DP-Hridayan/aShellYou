@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class
)

package `in`.hridayan.ashell.settings.presentation.page.crashhistory.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalAnimatedContentScope
import `in`.hridayan.ashell.core.common.LocalSharedTransitionScope
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.crashreporter.domain.model.CrashReport
import `in`.hridayan.ashell.crashreporter.presentation.viewmodel.CrashViewModel
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold
import `in`.hridayan.ashell.settings.presentation.components.shape.getRoundedShape
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CrashHistoryScreen(
    modifier: Modifier = Modifier,
    crashViewModel: CrashViewModel = hiltViewModel()
) {
    val listState = rememberLazyListState()
    val crashLogs by crashViewModel.crashLogs.collectAsState(initial = emptyList())
    val animatedContentScope = LocalAnimatedContentScope.current
    val sharedTransitionScope = LocalSharedTransitionScope.current

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.crash_history),
        content = { innerPadding, topBarScrollBehavior ->

            if (crashLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    NoCrashLogsUi()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                    contentPadding = innerPadding
                ) {
                    item {
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                        )
                    }

                    itemsIndexed(crashLogs) { index, crash ->
                        val shape = getRoundedShape(index, crashLogs.size)

                        with(sharedTransitionScope) {
                            CrashCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp, vertical = 1.dp),
                                crashReport = crash,
                                roundedShape = shape,
                                index = index,
                                animatedVisibilityScope = animatedContentScope
                            )
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
        })
}

@Composable
fun NoCrashLogsUi(modifier: Modifier = Modifier) {
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
                painter = painterResource(R.drawable.ic_check_circle),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = null
            )
        }

        AutoResizeableText(
            text = stringResource(R.string.great_news),
            style = MaterialTheme.typography.titleLargeEmphasized,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 50.dp, bottom = 15.dp)
        )

        Text(
            text = stringResource(R.string.no_crashes),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SharedTransitionScope.CrashCard(
    modifier: Modifier = Modifier,
    crashReport: CrashReport,
    roundedShape: RoundedCornerShape,
    animatedVisibilityScope: AnimatedVisibilityScope,
    index: Int,
    crashViewModel: CrashViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val weakHaptic = LocalWeakHaptic.current
    val timestamp = formatTimestamp(crashReport.timestamp)
    val crashTitle = getCrashTitle(crashReport.stackTrace)
    val sharedElementKey = "crashCardToCrashDetails$index"

    Card(
        modifier = modifier.sharedElement(
            sharedContentState = rememberSharedContentState(key = sharedElementKey),
            animatedVisibilityScope = animatedVisibilityScope
        ),
        shape = roundedShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = {
            weakHaptic()
            crashViewModel.setSharedElementKey(sharedElementKey)
            crashViewModel.setViewingCrash(crashReport)
            navController.navigate(NavRoutes.CrashDetailsScreen)
        }) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_bug),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentDescription = null
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                AutoResizeableText(
                    text = timestamp,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = crashTitle,
                    style = MaterialTheme.typography.bodyMediumEmphasized,
                    modifier = Modifier.alpha(0.9f)
                )
            }
        }

    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private fun getCrashTitle(stackTrace: String): String {
    val firstLine = stackTrace.lineSequence().firstOrNull() ?: "Unknown Crash"
    val title = firstLine.substringBefore(":").substringAfterLast(".").trim()

    return title.replace(Regex("(?<!^)([A-Z])"), " $1").trim()
}

