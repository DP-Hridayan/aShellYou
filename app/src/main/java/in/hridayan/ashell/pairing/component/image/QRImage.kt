package `in`.hridayan.ashell.pairing.component.image

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun QRImage(
    modifier: Modifier = Modifier,
    qrBitmap: Bitmap
) {
    val qrImage = qrBitmap.asImageBitmap()

    Box(
        modifier = modifier
            .background(Color.White)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = qrImage,
            contentDescription = "QR Code",
            modifier = Modifier
                .size(200.dp)
                .padding(25.dp)
        )
    }
}