package `in`.hridayan.ashell.core.presentation.model

import androidx.annotation.DrawableRes
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ButtonGroupItem(
    val onClick: () -> Unit = {},
    val text: String = "",
    val icon: ImageVector? = null,
    @param:DrawableRes val iconResId: Int? = null,
    val buttonConfig: ButtonConfig = ButtonConfig(),
    val contentDescription: String? = null,
    val enabled: Boolean = true
)

data class ButtonConfig(
    val type: ButtonType = ButtonType.Button,
    val colors: ButtonColors? = null,
    val outlinedButtonColors: ButtonColors? = null,
    val iconButtonConfig: ButtonGroupIconButtonConfig? = null,
)

enum class ButtonType {
    Button,
    OutlinedButton,
    IconButton,
    TextButton
}

object ButtonConfigDefaults {
    @Composable
    fun defaultConfig(
        type: ButtonType = ButtonType.Button,
        colors: ButtonColors = ButtonDefaults.buttonColors(),
        outlinedButtonColors: ButtonColors? = ButtonDefaults.outlinedButtonColors(),
        iconButtonConfig: ButtonGroupIconButtonConfig = ButtonGroupIconButtonDefaults.defaultConfig()
    ) = ButtonConfig(
        type = type,
        colors = colors,
        outlinedButtonColors = outlinedButtonColors,
        iconButtonConfig = iconButtonConfig,
    )
}

data class ButtonGroupIconButtonConfig(
    val containerSize: Dp? = 40.dp,
    val iconSize: Dp? = 20.dp,
    val colors: IconButtonColors? = null
)

object ButtonGroupIconButtonDefaults {
    @Composable
    fun defaultConfig(
        containerSize: Dp? = 40.dp,
        iconSize: Dp? = 20.dp,
        colors: IconButtonColors = IconButtonDefaults.iconButtonColors()
    ) = ButtonGroupIconButtonConfig(
        containerSize = containerSize,
        iconSize = iconSize,
        colors = colors
    )
}