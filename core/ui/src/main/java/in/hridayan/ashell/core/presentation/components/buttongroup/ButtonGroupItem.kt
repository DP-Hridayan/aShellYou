package `in`.hridayan.ashell.core.presentation.components.buttongroup

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType

@Composable
fun ButtonGroupItem(
    modifier: Modifier = Modifier,
    buttonGroupItem: ButtonGroupItem,
    interactionSource: MutableInteractionSource? = null,
) {
    with(buttonGroupItem) {
        when (buttonConfig.type) {
            ButtonType.Button -> Button(
                modifier = modifier,
                enabled = enabled,
                onClick = withHaptic { onClick() },
                shapes = ButtonDefaults.shapes(),
                colors = buttonConfig.colors ?: ButtonDefaults.buttonColors(),
                interactionSource = interactionSource,
                content = {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = contentDescription
                        )

                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    } else if (iconResId != null) {
                        Icon(
                            painter = painterResource(iconResId),
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    }

                    if (text.isNotEmpty()) {
                        AutoResizeableText(text = text)
                    }
                }
            )

            ButtonType.OutlinedButton -> OutlinedButton(
                modifier = modifier,
                enabled = enabled,
                onClick = withHaptic { onClick() },
                shapes = ButtonDefaults.shapes(),
                colors = buttonConfig.outlinedButtonColors ?: ButtonDefaults.outlinedButtonColors(),
                interactionSource = interactionSource,
                content = {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = contentDescription
                        )

                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    } else if (iconResId != null) {
                        Icon(
                            painter = painterResource(iconResId),
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    }

                    if (text.isNotEmpty()) {
                        AutoResizeableText(text = text)
                    }
                }
            )

            ButtonType.IconButton -> IconButton(
                modifier = modifier.then(
                    if (buttonConfig.iconButtonConfig?.containerSize != null)
                        Modifier.size(buttonConfig.iconButtonConfig.containerSize)
                    else Modifier
                ),
                enabled = enabled,
                onClick = withHaptic { onClick() },
                shapes = IconButtonDefaults.shapes(),
                colors = buttonConfig.iconButtonConfig?.colors
                    ?: IconButtonDefaults.iconButtonColors(),
                interactionSource = interactionSource,
                content = {
                    if (icon != null) {
                        Icon(
                            modifier = Modifier.then(
                                if (buttonConfig.iconButtonConfig?.iconSize != null)
                                    Modifier.size(buttonConfig.iconButtonConfig.iconSize)
                                else Modifier
                            ),
                            imageVector = icon,
                            contentDescription = contentDescription
                        )

                    } else if (iconResId != null) {
                        Icon(
                            modifier = Modifier.then(
                                if (buttonConfig.iconButtonConfig?.iconSize != null)
                                    Modifier.size(buttonConfig.iconButtonConfig.iconSize)
                                else Modifier
                            ),
                            painter = painterResource(iconResId),
                            contentDescription = null
                        )
                    }
                }
            )

            ButtonType.TextButton -> TextButton(
                modifier = modifier,
                enabled = enabled,
                onClick = withHaptic { onClick() },
                shapes = ButtonDefaults.shapes(),
                colors = buttonConfig.colors ?: ButtonDefaults.buttonColors(),
                interactionSource = interactionSource,
                content = {
                    if (text.isNotEmpty()) {
                        AutoResizeableText(text = text)
                    }
                }
            )
        }
    }
}