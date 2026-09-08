@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package `in`.hridayan.ashell.ui.screens

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.common.settings.LocalSettings
import `in`.hridayan.ashell.core.common.settings.SettingsKeys
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.core.ui.biometric.BiometricError
import `in`.hridayan.ashell.core.ui.biometric.BiometricPromptManager
import `in`.hridayan.ashell.core.utils.showToast

@Composable
fun AppLockedScreen(onUnlockSuccess: () -> Unit) {
    val context = LocalContext.current
    val res = LocalResources.current

    val settings = LocalSettings.current

    var isError by remember { mutableStateOf(true) }

    val onTriggerBiometricPrompt: () -> Unit = {
        triggerBiometricPrompt(
            context = context,
            title = res.getString(R.string.biometric_prompt_title),
            description = res.getString(R.string.biometric_prompt_description),
            onSuccess = {
                isError = false
                onUnlockSuccess()
            },
            onError = { error ->
                when (error) {
                    BiometricError.NoneEnrolled,
                    BiometricError.NoHardware -> {
                        showToast(context, res.getString(R.string.app_lock_disabled_msg))
                        isError = false
                        onUnlockSuccess()
                        settings.set(SettingsKeys.RequireAuthentication, false)
                    }

                    else -> isError = true
                }
            })
    }

    LaunchedEffect(Unit) {
        onTriggerBiometricPrompt()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ScatteredShapesBackground()

            Column(
                modifier = Modifier.padding(25.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(MaterialShapes.Cookie9Sided.toShape())
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isError) R.drawable.ic_lock else R.drawable.ic_lock_open),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(74.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.app_is_locked),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )

                Text(
                    modifier = Modifier.padding(horizontal = 15.dp),
                    text = stringResource(R.string.app_is_locked_msg),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                val size = ButtonDefaults.LargeContainerHeight

                Spacer(modifier = Modifier.heightIn(min = 80.dp))

                Button(
                    modifier = Modifier.heightIn(size),
                    contentPadding = ButtonDefaults.contentPaddingFor(
                        buttonHeight = size,
                        hasStartIcon = true
                    ),
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic { onTriggerBiometricPrompt() }
                ) {
                    Icon(
                        modifier = Modifier.size(ButtonDefaults.iconSizeFor(size)),
                        painter = painterResource(R.drawable.ic_fingerprint),
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.size(ButtonDefaults.iconSpacingFor(size)))

                    Text(
                        text = stringResource(R.string.unlock),
                        style = ButtonDefaults.textStyleFor(size)
                    )
                }
            }
        }
    }
}

private fun triggerBiometricPrompt(
    context: Context,
    title: String,
    description: String,
    onSuccess: () -> Unit,
    onError: (BiometricError) -> Unit
) {
    val activity = context as? AppCompatActivity ?: return

    BiometricPromptManager(activity).showBiometricPrompt(
        title = title,
        description = description,
        onSuccess = onSuccess,
        onError = { onError(it) })
}

@Composable
private fun ScatteredShapesBackground() {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .offset(x = (-40).dp, y = (-20).dp)
                .size(width = 200.dp, height = 100.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    shape = MaterialShapes.Pill.toShape()
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = MaterialShapes.Pill.toShape()
                    )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = 80.dp)
                .size(120.dp)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                    shape = MaterialShapes.Clover4Leaf.toShape()
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-30).dp, y = (-60).dp)
                .size(160.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                    shape = MaterialShapes.Cookie6Sided.toShape()
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                        shape = MaterialShapes.Cookie6Sided.toShape()
                    )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = (-120).dp)
                .size(width = 80.dp, height = 120.dp)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    shape = MaterialShapes.Square.toShape()
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 30.dp, y = 150.dp)
                .size(100.dp)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                    shape = MaterialShapes.SoftBurst.toShape()
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 20.dp, y = (-40).dp)
                .size(140.dp)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                    shape = MaterialShapes.Cookie12Sided.toShape()
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                        shape = MaterialShapes.Cookie12Sided.toShape()
                    )
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 40.dp, y = (-20).dp)
                .size(80.dp)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = MaterialShapes.Sunny.toShape()
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 100.dp, y = 120.dp)
                .size(60.dp)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                    shape = MaterialShapes.Diamond.toShape()
                )
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = (-10).dp, y = (-40).dp)
                .size(90.dp)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                    shape = MaterialShapes.Pill.toShape()
                )
        )
    }
}