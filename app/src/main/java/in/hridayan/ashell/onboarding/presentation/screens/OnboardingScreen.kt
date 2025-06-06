@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.onboarding.presentation.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.undrawPhoneLady
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.navigation.HomeScreen
import `in`.hridayan.ashell.navigation.LocalNavController
import `in`.hridayan.ashell.settings.data.local.SettingsKeys
import `in`.hridayan.ashell.settings.presentation.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(settingsViewModel: SettingsViewModel = hiltViewModel()) {
    val weakHaptic = LocalWeakHaptic.current
    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    val navController = LocalNavController.current

    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> PageOne()
                        1 -> PageTwo()
                        2 -> PageThree()
                        else -> {}
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            ) {
                AnimatedVisibility(
                    visible = pagerState.currentPage != 0,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        initialScale = 0f
                    ) + fadeIn(animationSpec = tween(150)),
                    exit = scaleOut(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy
                        ),
                        targetScale = 0f
                    ) + fadeOut(animationSpec = tween(150))
                ) {
                    TextButton(
                        onClick = {
                            weakHaptic()
                            coroutineScope.launch {
                                if (pagerState.currentPage > 0) pagerState.animateScrollToPage(
                                    pagerState.currentPage - 1
                                )
                            }
                        },
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(
                            text = stringResource(R.string.back),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        weakHaptic()
                        coroutineScope.launch {
                            if (pagerState.currentPage < pageCount - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            } else {
                                navController.navigate(HomeScreen)
                                settingsViewModel.setBoolean(SettingsKeys.FIRST_LAUNCH, false)
                            }
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.animateContentSize()
                ) {
                    Text(
                        text = if (pagerState.currentPage == pageCount - 1) stringResource(R.string.start)
                        else stringResource(R.string.btn_continue),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun PageOne() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .rotate(15f)
                .align(Alignment.Center)
                .clip(MaterialShapes.Puffy.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
        )

        Box(
            modifier = Modifier
                .size(65.dp)
                .rotate(15f)
                .align(Alignment.BottomCenter)
                .offset(y = (-120).dp, x = (30).dp)
                .clip(MaterialShapes.Puffy.toShape())
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .size(70.dp)
                .rotate(15f)
                .align(Alignment.BottomStart)
                .offset(y = (-40).dp, x = (10).dp)
                .clip(MaterialShapes.Arrow.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        )


        Box(
            modifier = Modifier
                .size(160.dp)
                .rotate(15f)
                .align(Alignment.CenterStart)
                .offset(y = (-100).dp, x = (-100).dp)
                .clip(MaterialShapes.Cookie7Sided.toShape())
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .rotate(15f)
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp, x = (-20).dp)
                .clip(MaterialShapes.Pill.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .rotate(15f)
                .align(Alignment.CenterEnd)
                .offset(y = (-145).dp, x = (-50).dp)
                .clip(MaterialShapes.Cookie4Sided.toShape())
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            Image(
                imageVector = DynamicColorImageVectors.undrawPhoneLady(),
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 70.dp)
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 75.dp)
                .align(Alignment.TopCenter)
                .clip(MaterialShapes.Oval.toShape())
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(35.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 35.dp)
                )

                AutoResizeableText(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 35.dp)
                )
            }
        }
    }
}

@Composable
fun PageTwo() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("This is Page 2.")
    }
}

@Composable
fun PageThree() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("You're on Page 3! 🎉")
    }
}