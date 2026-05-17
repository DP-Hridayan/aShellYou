package `in`.hridayan.ashell.settings.presentation.components.card

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.presentation.components.card.CustomCard
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.bmcLogo
import `in`.hridayan.ashell.core.presentation.theme.CardCornerShape
import `in`.hridayan.ashell.core.utils.openUrl

@Composable
fun SupportMeCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
    ) {
        ContactHandles(modifier = Modifier.fillMaxWidth())
        BuyMeACoffee(
            onClick = { openUrl(UrlConst.URL_DEV_BM_COFFEE, context) },
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun ContactHandles(
    modifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
    )
) {
    val context = LocalContext.current

    CustomCard(
        modifier = modifier,
        shape = CardCornerShape.FIRST_CARD,
        colors = colors
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 25.dp, vertical = 10.dp)
                .height(IntrinsicSize.Min)
        ) {
            ContactBox(
                modifier = Modifier.weight(1f),
                painter = painterResource(R.drawable.ic_mail),
                text = "E-mail",
                onClick = { openUrl(url = UrlConst.URL_DEV_EMAIL, context = context) }
            )

            VerticalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.padding(horizontal = 5.dp)
            )

            ContactBox(
                modifier = Modifier.weight(1f),
                painter = painterResource(R.drawable.ic_github),
                text = stringResource(R.string.github),
                onClick = { openUrl(url = UrlConst.URL_DEV_GITHUB, context = context) }
            )

            VerticalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.padding(horizontal = 5.dp)
            )

            ContactBox(
                modifier = Modifier.weight(1f),
                painter = painterResource(R.drawable.ic_telegram),
                text = "Telegram",
                onClick = { openUrl(url = UrlConst.URL_DEV_TELEGRAM, context = context) }
            )
        }
    }
}

@Composable
private fun BuyMeACoffee(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
) {
    val cardShape = CardCornerShape.LAST_CARD

    CustomCard(
        modifier = modifier,
        shape = cardShape,
        onClick = withHaptic { onClick() },
        colors = colors
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .premiumShine()
                .padding(
                    horizontal = 25.dp,
                    vertical = 15.dp
                )
        ) {
            Spacer(modifier = Modifier.weight(0.4f))

            Image(
                imageVector = DynamicColorImageVectors.bmcLogo(),
                contentDescription = null,
                modifier = Modifier.height(40.dp)
            )

            Text(
                text = stringResource(R.string.support_bmc),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(6f)
            )
        }
    }
}

fun Modifier.premiumShine(
    durationMillis: Int = 6500,
    pauseBetween: Int = 3000
): Modifier = composed {

    val infiniteTransition = rememberInfiniteTransition(
        label = "shineTransition"
    )

    val progress by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(pauseBetween)
        ),
        label = "shineProgress"
    )

    drawWithContent {

        drawContent()

        val width = size.width
        val height = size.height

        /*
         * Sharp highlight line
         */
        val sharpWidth = width * 0.12f

        val sharpStart = (progress * width * 1.8f)
        val sharpEnd = sharpStart + sharpWidth

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.05f),
                    Color.White.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                start = Offset(sharpStart, 0f),
                end = Offset(sharpEnd + 180f, height)
            ),
            blendMode = BlendMode.Screen
        )
    }
}

@Composable
private fun ContactBox(
    modifier: Modifier = Modifier,
    painter: Painter,
    text: String,
    tint: Color = MaterialTheme.colorScheme.onTertiaryContainer,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = withHaptic {
                onClick()
            })
            .padding(vertical = 5.dp),
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painter,
            colorFilter = ColorFilter.tint(tint),
            contentDescription = null,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}