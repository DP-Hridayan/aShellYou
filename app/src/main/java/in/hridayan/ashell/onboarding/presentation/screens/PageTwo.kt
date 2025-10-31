@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.onboarding.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.onboarding.presentation.component.item.DisclaimerItemLayout

@Composable
fun PageTwo(modifier: Modifier = Modifier, pagerState: PagerState) {
    val lazyListState = rememberLazyListState()

    val scale = remember { Animatable(0f) }
    val scaleMainShape = remember { Animatable(0.75f) }

    val disclaimerList = listOf(
        stringResource(R.string.disclaimer_1_title) to stringResource(R.string.disclaimer_1_description),
        stringResource(R.string.disclaimer_2_title) to stringResource(R.string.disclaimer_2_description),
        stringResource(R.string.disclaimer_3_title) to stringResource(R.string.disclaimer_3_description),
        stringResource(R.string.disclaimer_4_title) to stringResource(R.string.disclaimer_4_description),
        stringResource(R.string.disclaimer_5_title) to stringResource(R.string.disclaimer_5_description),
    )

    LaunchedEffect(pagerState.currentPage == 1) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(pagerState.currentPage == 1) {
        scaleMainShape.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .rotate(15f)
                .align(Alignment.Center)
                .scale(scale.value)
                .clip(MaterialShapes.Cookie12Sided.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
        )

        Box(
            modifier = Modifier
                .size(65.dp)
                .rotate(15f)
                .align(Alignment.BottomCenter)
                .offset(y = (-120).dp, x = (30).dp)
                .scale(scale.value)
                .clip(MaterialShapes.SoftBoom.toShape())
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .size(70.dp)
                .rotate(15f)
                .align(Alignment.BottomStart)
                .offset(y = (-100).dp, x = (10).dp)
                .scale(scale.value)
                .clip(MaterialShapes.PuffyDiamond.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .rotate(15f)
                .align(Alignment.CenterStart)
                .offset(y = (-180).dp, x = (-20).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Pentagon.toShape())
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .rotate(15f)
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp, x = (-20).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Bun.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .rotate(15f)
                .align(Alignment.CenterEnd)
                .offset(y = (-160).dp, x = (-50).dp)
                .scale(scale.value)
                .clip(MaterialShapes.SemiCircle.toShape())
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Box(
                    modifier = modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 65.dp, start = 20.dp, end = 20.dp)
                            .scale(scaleMainShape.value)
                            .clip(MaterialShapes.ClamShell.toShape())
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
                        contentAlignment = Alignment.Center
                    ) {
                        AutoResizeableText(
                            text = stringResource(R.string.disclaimer),
                            style = MaterialTheme.typography.headlineMediumEmphasized,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(35.dp)
                        )
                    }
                }
            }

            itemsIndexed(disclaimerList) { i, item ->
                DisclaimerItemLayout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    disclaimerItem = item
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(25.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
    }
}