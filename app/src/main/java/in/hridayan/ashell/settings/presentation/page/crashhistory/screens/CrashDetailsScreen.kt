@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class
)

package `in`.hridayan.ashell.settings.presentation.page.crashhistory.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.common.constants.DEV_EMAIL
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.ui.utils.ToastUtils.makeToast
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
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val weakHaptic = LocalWeakHaptic.current
    val navController = LocalNavController.current
    val parentEntry = remember(navController) {
        navController.getBackStackEntry(NavRoutes.CrashHistoryScreen)
    }
    val crashViewModel: CrashViewModel = hiltViewModel(parentEntry)
    val crash = crashViewModel.crash.value
    val stacktrace = crash?.stackTrace
    val deviceBrand = crash?.deviceBrand
    val deviceModel = crash?.deviceModel
    val manufacturer = crash?.manufacturer
    val androidVersion = crash?.osVersion
    val socManufacturer = crash?.socManufacturer
    val cpuAbi = crash?.cpuAbi
    val packageName = crash?.appPackageName
    val versionName = crash?.appVersionName
    val versionCode = crash?.appVersionCode
    val sharedElementKey = crashViewModel.sharedElementKey.value

    val deviceInfo =
        "Brand: $deviceBrand\nModel: $deviceModel\nManufacturer: $manufacturer\nAndroid version: $androidVersion\nSOC manufacturer: $socManufacturer\nCPU abi: $cpuAbi"

    val appInfo = "Package: $packageName\nVersion name: $versionName\nVersion code: $versionCode"

    SettingsScaffold(
        modifier = modifier,
        listState = listState,
        topBarTitle = stringResource(R.string.crash_details),
        fabContent = {
            ExtendedFloatingActionButton(
                onClick = {
                    weakHaptic()
                    context.sendCrashReport("$deviceInfo\n\n$appInfo\n\n$stacktrace")
                },
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
                        modifier = Modifier.padding(15.dp)
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 15.dp)
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
                                    text = deviceInfo,
                                    style = MaterialTheme.typography.bodySmallEmphasized
                                )
                            }

                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp, top = 5.dp, bottom = 15.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(36.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
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
                                painter = painterResource(R.drawable.ic_adb2),
                                contentDescription = null
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = appInfo,
                                    style = MaterialTheme.typography.bodySmallEmphasized
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
                            SelectionContainer {
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

private fun Context.sendCrashReport(log: String) {
    val subject = "Crash Report"
    val to = DEV_EMAIL

    try {
        val uriText = "mailto:$to?subject=${Uri.encode(subject)}&body=${log}"
        val uri = uriText.toUri()
        val emailIntent = Intent(Intent.ACTION_SENDTO, uri)
        startActivity(Intent.createChooser(emailIntent, "Send email using..."))
    } catch (e: Exception) {
        makeToast(this, "Failed to encode email content. ${e.toString()}")
    }
}
