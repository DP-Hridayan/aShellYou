@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.onboarding.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.undrawPhoneLady
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.onboarding.presentation.component.shape.DecorativeShape
import kotlinx.coroutines.launch

@Composable
fun PageOne(modifier: Modifier = Modifier, pagerState: PagerState) {
    val scale = remember { Animatable(0f) }
    val scaleMainShape = remember { Animatable(0.75f) }
    val ovalShape = MaterialShapes.Oval.toShape()

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 0) {
            launch {
                scale.animateTo(
                    1f,
                    tween(1500, easing = FastOutSlowInEasing)
                )
            }
            launch {
                scaleMainShape.animateTo(
                    1f,
                    tween(1500, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        DecorativeShape(
            size = 250,
            shape = MaterialShapes.Puffy.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
            scale = scale.value,
            modifier = Modifier.align(Alignment.Center)
        )

        DecorativeShape(
            size = 65,
            shape = MaterialShapes.Puffy.toShape(),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-120).dp, x = 30.dp)
        )

        DecorativeShape(
            size = 70,
            shape = MaterialShapes.Arrow.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = (-40).dp, x = 10.dp)
        )

        DecorativeShape(
            size = 160,
            shape = MaterialShapes.Cookie7Sided.toShape(),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = (-100).dp, x = (-100).dp)
        )

        DecorativeShape(
            size = 140,
            shape = MaterialShapes.Pill.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp, x = (-20).dp)
        )

        DecorativeShape(
            size = 100,
            shape = MaterialShapes.Cookie4Sided.toShape(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(y = (-145).dp, x = (-50).dp)
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
                .padding(top = 65.dp)
                .align(Alignment.TopCenter)
                .graphicsLayer {
                    scaleX = scaleMainShape.value
                    scaleY = scaleMainShape.value
                    shape = ovalShape
                    clip = true
                }
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