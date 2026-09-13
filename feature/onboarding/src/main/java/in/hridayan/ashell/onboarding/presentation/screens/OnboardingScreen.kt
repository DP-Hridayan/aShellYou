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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.NavRoutes
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.onboarding.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = hiltViewModel()) {
    val navController = LocalNavController.current
    val weakHaptic = LocalWeakHaptic.current

    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    var isLegalAccepted by remember { mutableStateOf(false) }

    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 35.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (pagerState.currentPage == 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isLegalAccepted,
                            onCheckedChange = {
                                isLegalAccepted = it
                                weakHaptic()
                            }
                        )

                        val privacyText = stringResource(R.string.privacy_policy)
                        val tosText = stringResource(R.string.terms_of_service)
                        val fullText =
                            stringResource(R.string.legal_consent_text, privacyText, tosText)

                        val privacyStartIndex = fullText.indexOf(privacyText)
                        val tosStartIndex = fullText.indexOf(tosText)

                        val primaryColor = MaterialTheme.colorScheme.primary

                        val annotatedString = buildAnnotatedString {
                            append(fullText)

                            if (privacyStartIndex >= 0) {
                                addLink(
                                    androidx.compose.ui.text.LinkAnnotation.Clickable(
                                        tag = "PRIVACY",
                                        linkInteractionListener = {
                                            navController.navigate(NavRoutes.PrivacyPolicyScreen)
                                            weakHaptic()
                                        }
                                    ),
                                    start = privacyStartIndex,
                                    end = privacyStartIndex + privacyText.length
                                )

                                addStyle(
                                    style = SpanStyle(
                                        color = primaryColor,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    start = privacyStartIndex,
                                    end = privacyStartIndex + privacyText.length
                                )
                            }

                            if (tosStartIndex >= 0) {
                                addLink(
                                    androidx.compose.ui.text.LinkAnnotation.Clickable(
                                        tag = "TOS",
                                        linkInteractionListener = {
                                            navController.navigate(NavRoutes.TermsOfServiceScreen)
                                            weakHaptic()
                                        }
                                    ),
                                    start = tosStartIndex,
                                    end = tosStartIndex + tosText.length
                                )

                                addStyle(
                                    style = SpanStyle(
                                        color = primaryColor,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    start = tosStartIndex,
                                    end = tosStartIndex + tosText.length
                                )
                            }
                        }

                        Text(
                            text = annotatedString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
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
                            onClick = withHaptic {
                                coroutineScope.launch {
                                    if (pagerState.currentPage > 0) {
                                        pagerState.animateScrollToPage(
                                            pagerState.currentPage - 1
                                        )
                                    }
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

                    NextButton(
                        enabled = if (pagerState.currentPage == 1) isLegalAccepted else true,
                        pagerState = pagerState,
                        pageCount = pageCount,
                        onClick = {
                            coroutineScope.launch {
                                if (pagerState.currentPage < pageCount - 1) {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                } else {
                                    viewModel.completeOnboarding()
                                    navController.navigate(NavRoutes.HomeScreen) {
                                        popUpTo(NavRoutes.OnboardingScreen) { inclusive = true }
                                    }
                                }
                            }
                        })

                }
            }
        }) { contentPadding ->

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            contentPadding = contentPadding,
            userScrollEnabled = if (pagerState.currentPage == 1) isLegalAccepted else true
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                when (page) {
                    0 -> PageOne(pagerState = pagerState)
                    1 -> PageTwo(pagerState = pagerState)
                    2 -> PageThree(pagerState = pagerState)
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun NextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    pagerState: PagerState,
    pageCount: Int,
    onClick: () -> Unit = {}
) {
    Button(
        enabled = enabled,
        onClick = withHaptic { onClick() },
        shapes = ButtonDefaults.shapes(),
        modifier = modifier.animateContentSize()
    ) {
        Text(
            text = if (pagerState.currentPage == pageCount - 1) {
                stringResource(R.string.start)
            } else {
                stringResource(R.string.btn_continue)
            },
            modifier = Modifier.padding(horizontal = 10.dp)
        )
    }
}

