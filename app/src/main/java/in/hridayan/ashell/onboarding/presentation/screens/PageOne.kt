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
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.undrawPhoneLady
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

@Composable
fun PageOne(modifier: Modifier = Modifier) {
    var scale = remember { Animatable(0f) }

    var scaleMainShape = remember { Animatable(0.75f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(Unit) {
        scaleMainShape.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .rotate(15f)
                .align(Alignment.Center)
                .scale(scale.value)
                .clip(MaterialShapes.Puffy.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f))
        )

        Box(
            modifier = Modifier
                .size(65.dp)
                .rotate(15f)
                .align(Alignment.BottomCenter)
                .offset(y = (-120).dp, x = (30).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Puffy.toShape())
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .size(70.dp)
                .rotate(15f)
                .align(Alignment.BottomStart)
                .offset(y = (-40).dp, x = (10).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Arrow.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        )


        Box(
            modifier = Modifier
                .size(160.dp)
                .rotate(15f)
                .align(Alignment.CenterStart)
                .offset(y = (-100).dp, x = (-100).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Cookie7Sided.toShape())
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .size(140.dp)
                .rotate(15f)
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp, x = (-20).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Pill.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .rotate(15f)
                .align(Alignment.CenterEnd)
                .offset(y = (-145).dp, x = (-50).dp)
                .scale(scale.value)
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
                .padding(top = 65.dp)
                .align(Alignment.TopCenter)
                .scale(scaleMainShape.value)
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