package `in`.hridayan.ashell.settings.presentation.components.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.common.LocalWeakHaptic
import `in`.hridayan.ashell.core.common.constants.UrlConst
import `in`.hridayan.ashell.core.presentation.components.svg.DynamicColorImageVectors
import `in`.hridayan.ashell.core.presentation.components.svg.vectors.bmcLogo
import `in`.hridayan.ashell.core.utils.openUrl

@Composable
fun SupportMeCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer
            )
            .clickable(enabled = true, onClick = {})
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ContactHandles()

            HorizontalDivider(thickness = 1.dp)

            BuyMeACoffee(onClick = { openUrl(UrlConst.URL_DEV_BM_COFFEE, context) })
        }
    }
}

@Composable
private fun ContactHandles(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .padding(horizontal = 25.dp, vertical = 10.dp)
            .height(IntrinsicSize.Min)
    ) {
        ContactBox(
            modifier = Modifier.weight(1f),
            painter = painterResource(R.drawable.ic_mail),
            text = "E-mail",
            onClick = { openUrl(url = UrlConst.URL_DEV_EMAIL, context = context) }
        )

        VerticalDivider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 5.dp))

        ContactBox(
            modifier = Modifier.weight(1f),
            painter = painterResource(R.drawable.ic_github),
            text = stringResource(R.string.github),
            onClick = { openUrl(url = UrlConst.URL_DEV_GITHUB, context = context) }
        )

        VerticalDivider(thickness = 1.dp, modifier = Modifier.padding(horizontal = 5.dp))

        ContactBox(
            modifier = Modifier.weight(1f),
            painter = painterResource(R.drawable.ic_telegram),
            text = "Telegram",
            onClick = { openUrl(url = UrlConst.URL_DEV_TELEGRAM, context = context) }
        )
    }
}

@Composable
private fun ContactBox(
    modifier: Modifier = Modifier,
    painter: Painter,
    text: String,
    onClick: () -> Unit = {}
) {
    val weakHaptic = LocalWeakHaptic.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(enabled = true, onClick = {
                onClick()
                weakHaptic()
            })
            .padding(vertical = 5.dp),
    ) {
        Image(
            modifier = Modifier.size(24.dp),
            painter = painter,
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondaryContainer),
            contentDescription = null,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BuyMeACoffee(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    val weakHaptic = LocalWeakHaptic.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable(enabled = true, onClick = {
                onClick()
                weakHaptic()
            })
            .padding(horizontal = 25.dp, vertical = 15.dp)
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