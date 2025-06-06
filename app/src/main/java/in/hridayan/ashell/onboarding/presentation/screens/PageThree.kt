@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.onboarding.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.undrawSelectChoice
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText

@Composable
fun PageThree(modifier: Modifier = Modifier, pagerState: PagerState) {
    val weakHaptic = LocalWeakHaptic.current

    var scale = remember { Animatable(0f) }

    var scaleMainShape = remember { Animatable(0.75f) }

    var rootCardChecked by rememberSaveable { mutableStateOf(false) }

    var shizukuCardChecked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(pagerState.currentPage) {
        scaleMainShape.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1500,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .size(250.dp)
                .rotate(15f)
                .align(Alignment.Center)
                .scale(scale.value)
                .clip(MaterialShapes.Clover8Leaf.toShape())
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
                .offset(y = (-100).dp, x = (10).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Fan.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        )

        Box(
            modifier = Modifier
                .size(80.dp)
                .rotate(15f)
                .align(Alignment.CenterStart)
                .offset(y = (-180).dp, x = (-20).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Flower.toShape())
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .size(120.dp)
                .rotate(15f)
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp, x = (-20).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Arch.toShape())
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .rotate(15f)
                .align(Alignment.CenterEnd)
                .offset(y = (-160).dp, x = (-50).dp)
                .scale(scale.value)
                .clip(MaterialShapes.Ghostish.toShape())
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 80.dp)
                .background(Color.Transparent)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 65.dp, start = 20.dp, end = 20.dp)
                    .align(Alignment.CenterHorizontally)
                    .scale(scaleMainShape.value)
                    .clip(MaterialShapes.SemiCircle.toShape())
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.grant_permission),
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        top = 55.dp,
                        bottom = 30.dp,
                        start = 20.dp,
                        end = 20.dp
                    )
                )
            }

            Image(
                imageVector = DynamicColorImageVectors.undrawSelectChoice(),
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 100.dp)
            )

            AutoResizeableText(
                text = stringResource(R.string.permission_optional),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            PermissionCard(
                isChecked = rootCardChecked,
                title = stringResource(R.string.root),
                description = stringResource(R.string.mode_one_desc),
                onClick = {
                    weakHaptic()
                    rootCardChecked = !rootCardChecked
                }
            )

            PermissionCard(
                isChecked = shizukuCardChecked,
                title = stringResource(R.string.shizuku),
                description = stringResource(R.string.mode_two_desc),
                onClick = {
                    weakHaptic()
                    shizukuCardChecked = !shizukuCardChecked
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .height(40.dp)
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

@Composable
private fun PermissionCard(
    modifier: Modifier = Modifier,
    isChecked: Boolean,
    onClick: () -> Unit,
    title: String,
    description: String
) {
    val weakHaptic = LocalWeakHaptic.current

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .clip(MaterialTheme.shapes.large)
            .border(CardDefaults.outlinedCardBorder())
            .clickable(enabled = true, onClick = {
                weakHaptic()
                onClick()
            }),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            contentColor = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Icon(
                imageVector = if (isChecked) Icons.Rounded.CheckCircle else Icons.Rounded.CheckCircleOutline,
                contentDescription = null,
                tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.5f
                ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.alpha(0.9f)
                )
            }
        }
    }
}