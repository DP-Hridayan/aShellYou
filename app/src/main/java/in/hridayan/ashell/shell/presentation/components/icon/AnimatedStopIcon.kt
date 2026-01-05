package `in`.hridayan.ashell.shell.presentation.components.icon

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.widget.ImageView
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import `in`.hridayan.ashell.R

@SuppressLint("UseCompatLoadingForDrawables")
@Composable
fun AnimatedStopIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onErrorContainer
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                val avd =
                    context.getDrawable(R.drawable.ic_stop_animated) as? AnimatedVectorDrawable
                setImageDrawable(avd)
                setColorFilter(tint.toArgb(), PorterDuff.Mode.SRC_IN)
                avd?.start()
            }
        },
        modifier = modifier.size(24.dp)
    )
}
