package `in`.hridayan.composecolorkit.palette

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.utilities.CorePalette
import `in`.hridayan.composecolorkit.provider.SeedColorProvider

private val primaryPalette get() = CorePalette.of(SeedColorProvider.primary.toArgb())
private val secondaryPalette get() = CorePalette.of(SeedColorProvider.secondary.toArgb())
private val tertiaryPalette get() = CorePalette.of(SeedColorProvider.tertiary.toArgb())

val Int.a1 get() = primaryPalette.a1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.a2 get() = secondaryPalette.a1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.a3 get() = tertiaryPalette.a1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.n1 get() = primaryPalette.n1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.n2 get() = primaryPalette.n2.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.error get() = primaryPalette.error.getHct(this.toDouble()).toInt().let { Color(it) }