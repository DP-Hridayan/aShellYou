@file:SuppressLint("RestrictedApi")

package `in`.hridayan.ashell.core.presentation.utils

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.CorePalette
import `in`.hridayan.ashell.core.common.SeedColorProvider

private val palette: CorePalette get() = CorePalette.of(SeedColorProvider.seedColor)

val Int.a1 get() = palette.a1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.a2 get() = palette.a2.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.a3 get() = palette.a3.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.n1 get() = palette.n1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.n2 get() = palette.n2.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.error get() = palette.error.getHct(this.toDouble()).toInt().let { Color(it) }
