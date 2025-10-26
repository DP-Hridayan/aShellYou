@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.BuildConfig
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.presentation.components.card.PillShapedCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import kotlinx.coroutines.launch

@Composable
fun LatestVersionDialog(onDismiss: () -> Unit) {
    val weakHaptic = LocalWeakHaptic.current

    val (angle, scale) = syncedRotationAndScale()

    val appVersionName = stringResource(R.string.version_name) + ": " + BuildConfig.VERSION_NAME
    val appVersionCode =
        stringResource(R.string.version_code) + ": " + BuildConfig.VERSION_CODE.toString()

    DialogContainer(onDismiss = onDismiss) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Spacer(
                modifier = Modifier
                    .requiredSize(96.dp)
                    .graphicsLayer {
                        rotationZ = angle
                    }
                    .scale(scale)
                    .clip(MaterialShapes.Cookie9Sided.toShape())
                    .clickable(enabled = true, onClick = weakHaptic)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Icon(
                painter = painterResource(R.drawable.ic_verified),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }

        Text(
            text = stringResource(R.string.already_latest_version),
            style = MaterialTheme.typography.titleSmallEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 20.dp)
        )

        PillShapedCard(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            AutoResizeableText(
                text = appVersionName,
                style = MaterialTheme.typography.bodySmallEmphasized,
                modifier = Modifier.padding(top = 10.dp, start = 15.dp, end = 15.dp)
            )
            AutoResizeableText(
                text = appVersionCode,
                style = MaterialTheme.typography.bodySmallEmphasized,
                modifier = Modifier.padding(top = 2.dp, bottom = 10.dp, start = 15.dp, end = 15.dp)
            )
        }
    }
}

@Composable
private fun syncedRotationAndScale(): Pair<Float, Float> {
    val rotation = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val durationMillis = 3000

    LaunchedEffect(Unit) {
        launch {
            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + 360f,
                    animationSpec = tween(
                        durationMillis = durationMillis,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
        launch {
            var toSmall = true
            while (true) {
                val target = if (toSmall) 0.7f else 1f
                toSmall = !toSmall
                scale.animateTo(
                    targetValue = target,
                    animationSpec = tween(
                        durationMillis = durationMillis,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }
    }

    return rotation.value to scale.value
}
