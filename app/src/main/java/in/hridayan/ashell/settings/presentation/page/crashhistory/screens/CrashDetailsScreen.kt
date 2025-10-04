@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class
)

package `in`.hridayan.ashell.settings.presentation.page.crashhistory.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.crashreporter.presentation.viewmodel.CrashViewModel
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.navigation.NavRoutes
import `in`.hridayan.ashell.settings.presentation.components.scaffold.SettingsScaffold

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun SharedTransitionScope.CrashDetailsScreen(
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val listState = rememberLazyListState()
    val navController = LocalNavController.current
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(NavRoutes.CrashHistoryScreen)
    }
    val crashViewModel: CrashViewModel = hiltViewModel(parentEntry)
    val crash = crashViewModel.crash.value
    val stacktrace = crash?.stackTrace
    val deviceName = crash?.deviceName
    val manufacturer = crash?.manufacturer
    val androidVersion = crash?.osVersion
    val sharedElementKey = crashViewModel.sharedElementKey.value

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.crash_details),
        fabContent = {
            ExtendedFloatingActionButton(
                onClick = {},
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_report),
                    contentDescription = null,
                    modifier = Modifier.padding(end = 10.dp)
                )
                AutoResizeableText(text = stringResource(R.string.report))
            }
        },
        content = { innerPadding, topBarScrollBehavior ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .nestedScroll(topBarScrollBehavior.nestedScrollConnection),
                contentPadding = innerPadding
            ) {
                item {
                    AutoResizeableText(
                        text = stringResource(R.string.device_info),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 15.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(sharedElementKey),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_mobile_info),
                                contentDescription = null
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.device_name) + ": $deviceName\n"
                                            + stringResource(R.string.manufacturer) + ": $manufacturer\n"
                                            + stringResource(R.string.android_version) + ": $androidVersion",
                                    style = MaterialTheme.typography.bodyMediumEmphasized
                                )
                            }

                        }
                    }

                    AutoResizeableText(
                        text = stringResource(R.string.stack_trace),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(
                            start = 20.dp,
                            end = 20.dp,
                            top = 25.dp,
                        )
                    )

                    stacktrace?.let {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = it,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 15.dp),
                                style = MaterialTheme.typography.bodySmallEmphasized,
                                fontFamily = FontFamily.Monospace,
                            )
                        }
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