package `in`.hridayan.ashell.core.presentation.battery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BatteryOrientation {
    HORIZONTAL,
    VERTICAL
}

@Composable
fun BatteryIndicator(
    level: Int,
    modifier: Modifier = Modifier,
    orientation: BatteryOrientation = BatteryOrientation.HORIZONTAL, // Defaults to horizontal
    showPercentageText: Boolean = true,
    isCharging: Boolean = false,
    lowColor: Color = MaterialTheme.colorScheme.error,
    normalColor: Color = MaterialTheme.colorScheme.primary,
    chargingColor: Color = MaterialTheme.colorScheme.tertiary
) {
    val clampedLevel = level.coerceIn(0, 100)
    val stateColor = when {
        isCharging -> chargingColor
        clampedLevel <= 20 -> lowColor
        else -> normalColor
    }

    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

    if (orientation == BatteryOrientation.HORIZONTAL) {
        // --- HORIZONTAL LAYOUT ---
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Body
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(52.dp)
                    .border(2.dp, outlineColor, RoundedCornerShape(4.dp))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Progress fills from left to right
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = clampedLevel / 100f)
                        .background(stateColor, RoundedCornerShape(1.5.dp))
                        .align(Alignment.CenterStart)
                )
                if (showPercentageText) {
                    Text(
                        text = "$clampedLevel%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.width(2.dp))
            // Tip on the right side
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 8.dp)
                    .background(outlineColor, RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
            )
        }
    } else {
        // --- VERTICAL LAYOUT ---
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tip on the top side
            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 3.dp)
                    .background(outlineColor, RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Main Body
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(52.dp)
                    .border(2.dp, outlineColor, RoundedCornerShape(4.dp))
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                // Progress fills from bottom to top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = clampedLevel / 100f)
                        .background(stateColor, RoundedCornerShape(1.5.dp))
                        .align(Alignment.BottomCenter)
                )
                if (showPercentageText) {
                    Text(
                        text = "$clampedLevel%",
                        fontSize = 9.sp, // Slightly smaller to fit vertical width safely
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}