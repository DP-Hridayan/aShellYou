@file:SuppressLint("RestrictedApi")

package `in`.hridayan.ashell.core.presentation.utils

import android.annotation.SuppressLint
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.CorePalette
import `in`.hridayan.ashell.core.common.SeedColorProvider

private val primaryPalette get() = CorePalette.of(SeedColorProvider.primary)
private val secondaryPalette get() = CorePalette.of(SeedColorProvider.secondary)
private val tertiaryPalette get() = CorePalette.of(SeedColorProvider.tertiary)

val Int.a1 get() = primaryPalette.a1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.a2 get() = secondaryPalette.a1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.a3 get() = tertiaryPalette.a1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.n1 get() = primaryPalette.n1.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.n2 get() = primaryPalette.n2.getHct(this.toDouble()).toInt().let { Color(it) }
val Int.error get() = primaryPalette.error.getHct(this.toDouble()).toInt().let { Color(it) }
