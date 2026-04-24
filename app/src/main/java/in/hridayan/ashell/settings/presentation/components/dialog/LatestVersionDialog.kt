@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.dialog

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.AshellYouAnimationSpecs
import `in`.hridayan.ashell.core.presentation.utils.syncedRotationAndScale

@Composable
fun LatestVersionDialog(onDismiss: () -> Unit) {
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
                    .clickable(onClick = withHaptic {})
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

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnimatedInfoCard(appVersionName)
            AnimatedInfoCard(appVersionCode)
        }
    }
}

@Composable
private fun AnimatedInfoCard(text: String) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val roundedCornerPercentage by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = AshellYouAnimationSpecs.springInt,
        label = "corner_anim"
    )

    val animatedRoundedCornerShape =
        RoundedCornerShape(roundedCornerPercentage.coerceIn(0, 100))

    Card(
        modifier = Modifier
            .clip(animatedRoundedCornerShape)
            .clickable(interactionSource = interactionSource) {},
        shape = animatedRoundedCornerShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        AutoResizeableText(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            text = text,
            style = MaterialTheme.typography.bodySmallEmphasized,
        )
    }
}