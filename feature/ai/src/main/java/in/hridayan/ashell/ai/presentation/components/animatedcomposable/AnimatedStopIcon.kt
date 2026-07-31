package `in`.hridayan.ashell.ai.presentation.components.animatedcomposable

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
import androidx.core.content.ContextCompat
import `in`.hridayan.ashell.core.resources.R

@Composable
fun AnimatedStopIcon(
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onErrorContainer
) {
    AndroidView(
        factory = { context ->
            ImageView(context).apply {
                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_stop_animated)
                val avd = drawable as? AnimatedVectorDrawable

                setImageDrawable(avd)
                setColorFilter(tint.toArgb(), PorterDuff.Mode.SRC_IN)
                avd?.start()
            }
        },
        update = {
            it.setColorFilter(tint.toArgb(), PorterDuff.Mode.SRC_IN)
        },
        modifier = modifier.size(24.dp)
    )
}