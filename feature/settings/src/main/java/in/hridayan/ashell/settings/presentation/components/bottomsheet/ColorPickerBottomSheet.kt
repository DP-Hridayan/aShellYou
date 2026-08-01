package `in`.hridayan.ashell.settings.presentation.components.bottomsheet

import android.graphics.Color.HSVToColor
import android.graphics.Color.colorToHSV
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.resources.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorPickerBottomSheet(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    var currentColor by remember { mutableStateOf(initialColor) }
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.pick_color),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            ColorWheel(
                colorProvider = { currentColor },
                onColorChange = { currentColor = it },
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            RgbSliders(
                colorProvider = { currentColor },
                onColorChange = { currentColor = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            HexInputRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .imePadding(),
                colorProvider = { currentColor },
                onColorChange = { currentColor = it }
            )

            Button(
                onClick = withHaptic {
                    onColorSelected(currentColor)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(stringResource(id = R.string.apply))
            }
        }
    }
}

@Composable
private fun ColorWheel(
    colorProvider: () -> Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    // We compute hsv dynamically in the pointer/draw block to avoid recomposition


    val updateColorFromPointer by rememberUpdatedState { offset: Offset, sz: IntSize ->
        val center = Offset(sz.width / 2f, sz.height / 2f)
        val radius = sz.width / 2f
        val dx = offset.x - center.x
        val dy = offset.y - center.y

        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        if (angle < 0) angle += 360f

        val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        val saturation = (distance / radius).coerceIn(0f, 1f)

        val currentHsv = FloatArray(3)
        colorToHSV(colorProvider().toArgb(), currentHsv)
        val currentValue = currentHsv[2]

        val newHsv = floatArrayOf(angle, saturation, currentValue)
        val newColorInt = HSVToColor(newHsv)
        onColorChange(Color(newColorInt))
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // 1. Static Gradient Wheel (only recomposes if size changes, never on color change)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            val colors = listOf(
                Color.Red, Color.Yellow, Color.Green, Color.Cyan,
                Color.Blue, Color.Magenta, Color.Red
            )
            drawCircle(
                brush = Brush.sweepGradient(colors, center),
                radius = radius,
                center = center
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }

        // 2. Dynamic Overlay and Pointer + Touch Input
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset -> updateColorFromPointer(offset, size) }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        updateColorFromPointer(change.position, size)
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f

            val color = colorProvider()
            val hsv = FloatArray(3)
            colorToHSV(color.toArgb(), hsv)

            // Draw Black overlay for Value (Brightness)
            val hue = hsv[0]
            val sat = hsv[1]
            val value = hsv[2]

            if (value < 1f) {
                drawCircle(
                    color = Color.Black.copy(alpha = 1f - value),
                    radius = radius,
                    center = center
                )
            }

            // Draw Pointer
            val angleRad = Math.toRadians(hue.toDouble())
            val pointerRadius = sat * radius
            val pointerX = center.x + (pointerRadius * cos(angleRad)).toFloat()
            val pointerY = center.y + (pointerRadius * sin(angleRad)).toFloat()

            drawCircle(
                color = Color.White,
                radius = 10.dp.toPx(),
                center = Offset(pointerX, pointerY),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color.Black,
                radius = 12.dp.toPx(),
                center = Offset(pointerX, pointerY),
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}

@Composable
private fun RgbSliders(
    colorProvider: () -> Color,
    onColorChange: (Color) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ColorSlider(label = "R", valueProvider = { colorProvider().red }, color = Color.Red) { r ->
            val c = colorProvider()
            onColorChange(c.copy(red = r))
        }
        ColorSlider(
            label = "G",
            valueProvider = { colorProvider().green },
            color = Color.Green
        ) { g ->
            val c = colorProvider()
            onColorChange(c.copy(green = g))
        }
        ColorSlider(
            label = "B",
            valueProvider = { colorProvider().blue },
            color = Color.Blue
        ) { b ->
            val c = colorProvider()
            onColorChange(c.copy(blue = b))
        }
    }
}

@Composable
private fun ColorSlider(
    label: String,
    valueProvider: () -> Float,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        AutoResizeableText(
            modifier = Modifier.width(24.dp),
            text = label,
            fontWeight = FontWeight.Bold
        )

        IsolatedSlider(
            valueProvider = valueProvider,
            onValueChange = onValueChange,
            color = color,
            modifier = Modifier.weight(1f)
        )

        IsolatedSliderText(valueProvider = valueProvider)
    }
}

@Composable
private fun IsolatedSlider(
    valueProvider: () -> Float,
    onValueChange: (Float) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Slider(
        value = valueProvider(),
        onValueChange = onValueChange,
        modifier = modifier,
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color
        )
    )
}

@Composable
private fun IsolatedSliderText(valueProvider: () -> Float) {
    AutoResizeableText(
        text = (valueProvider() * 255).roundToInt().toString(),
        modifier = Modifier.width(32.dp)
    )
}

@Composable
private fun HexInputRow(
    modifier: Modifier = Modifier,
    colorProvider: () -> Color,
    onColorChange: (Color) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        val outlineColor = MaterialTheme.colorScheme.outlineVariant
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .drawBehind {
                    drawRect(colorProvider())
                }
                .border(1.dp, outlineColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))

        IsolatedHexTextField(
            colorProvider = colorProvider,
            onColorChange = onColorChange,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun IsolatedHexTextField(
    colorProvider: () -> Color,
    onColorChange: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    fun Color.toHexString(): String {
        val r = (this.red * 255).roundToInt()
        val g = (this.green * 255).roundToInt()
        val b = (this.blue * 255).roundToInt()
        return String.format("#%02X%02X%02X", r, g, b)
    }

    val color = colorProvider()
    var hexText by remember { mutableStateOf(color.toHexString()) }
    var lastColor by remember { mutableStateOf(color) }

    if (color != lastColor) {
        lastColor = color
        hexText = color.toHexString()
    }

    OutlinedTextField(
        value = hexText,
        onValueChange = { newHex ->
            hexText = newHex
            try {
                val parsed = if (newHex.startsWith("#")) newHex else "#$newHex"
                val parsedColor = Color(parsed.toColorInt())
                lastColor = parsedColor
                onColorChange(parsedColor)
            } catch (e: Exception) {
                // Ignore invalid hex
            }
        },
        label = { Text("HEX") },
        modifier = modifier,
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
    )
}
