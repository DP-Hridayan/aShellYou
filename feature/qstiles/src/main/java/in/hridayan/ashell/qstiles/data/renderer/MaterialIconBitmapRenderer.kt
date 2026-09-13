package `in`.hridayan.ashell.qstiles.data.renderer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.createBitmap
import javax.inject.Inject

class MaterialIconBitmapRenderer @Inject constructor() {
    fun render(typeface: Typeface, codepoint: Int, sizePx: Int): Bitmap {
        val bitmap = createBitmap(sizePx, sizePx)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            isAntiAlias = true
            this.typeface = typeface
            textSize = sizePx * GLYPH_SIZE_RATIO
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
        }

        val text = String(Character.toChars(codepoint))
        val fontMetrics = paint.fontMetrics

        val xPos = sizePx / HALF_DIVISOR
        val yPos =
            (sizePx / HALF_DIVISOR) - ((fontMetrics.descent + fontMetrics.ascent) / HALF_DIVISOR)

        canvas.drawText(text, xPos, yPos, paint)

        return bitmap
    }

    companion object {
        private const val GLYPH_SIZE_RATIO = 0.75f
        private const val HALF_DIVISOR = 2f
    }
}
