@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.shell.wifi_adb_shell.presentation.component.image

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.shell.wifi_adb_shell.domain.model.WifiAdbState

@Composable
fun QRImage(
    modifier: Modifier = Modifier,
    qrBitmap: Bitmap,
    isWifiConnected: Boolean = false,
    wifiAdbState: WifiAdbState = WifiAdbState.None
) {
    val qrImage = qrBitmap.asImageBitmap()

    Box(
        modifier = modifier
            .size(200.dp)
            .clip(MaterialShapes.Cookie9Sided.toShape())
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = qrImage,
            contentDescription = "QR Code",
            modifier = Modifier.padding(50.dp)
        )

        if (!isWifiConnected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_no_wifi),
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(72.dp)
                )
            }

            return@Box
        }

        if (wifiAdbState is WifiAdbState.PairingStarted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.pairing),
                    color = Color.Black
                )
            }
        }

        if (wifiAdbState is WifiAdbState.ConnectStarted || wifiAdbState is WifiAdbState.DiscoveryFound || wifiAdbState is WifiAdbState.DiscoveryStarted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.connecting),
                    color = Color.Black,
                    style = MaterialTheme.typography.bodyLargeEmphasized,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}