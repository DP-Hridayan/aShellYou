@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.settings.presentation.components.dialog


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.dialog.DialogContainer
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.theme.CustomCardShape
import `in`.hridayan.ashell.core.presentation.utils.syncedRotationAndScale
import `in`.hridayan.ashell.core.resources.R

@Composable
fun LatestVersionDialog(onDismiss: () -> Unit) {
    val (angle, scale) = syncedRotationAndScale()
    val context = androidx.compose.ui.platform.LocalContext.current
    val appVersionName =
        stringResource(R.string.version_name) + ": " + (context.packageManager.getPackageInfo(
            context.packageName,
            0
        ).versionName ?: "unknown")

    val flavorDisplay = "GitHub"

    val appVersionCode = stringResource(R.string.variant) + ": " + flavorDisplay

    DialogContainer(onDismiss = onDismiss) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Spacer(
                modifier = Modifier
                    .requiredSize(96.dp)
                    .graphicsLayer {
                        rotationZ = angle()
                        scaleX = scale()
                        scaleY = scale()
                    }
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
            InfoChip(appVersionName)
            InfoChip(appVersionCode)
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    CustomCard(
        shape = CustomCardShape(50),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ),
        pressedCornerRadius = 8.dp
    ) {
        AutoResizeableText(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            text = text,
            style = MaterialTheme.typography.bodySmallEmphasized,
        )
    }
}
