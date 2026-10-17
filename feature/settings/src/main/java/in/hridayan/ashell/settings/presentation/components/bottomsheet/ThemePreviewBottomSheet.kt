@file:OptIn(ExperimentalMaterial3Api::class)

package `in`.hridayan.ashell.settings.presentation.components.bottomsheet

import android.graphics.Color.parseColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.theme.data.ColorSchemePayload
import `in`.hridayan.ashell.core.presentation.theme.data.UserGeneratedColorSchemeEntity
import `in`.hridayan.ashell.core.presentation.theme.data.toEntity
import `in`.hridayan.ashell.core.resources.R

private const val COLOR_HEX_PREFIX = "#"

private val PADDING_HORIZONTAL = 24.dp
private val PADDING_BOTTOM = 32.dp
private val SPACING_MEDIUM = 24.dp
private val SPACING_LARGE = 32.dp
private val PREVIEW_CARD_HEIGHT = 150.dp
private val PREVIEW_CARD_CORNER_RADIUS = 16.dp
private val PREVIEW_CARD_CONTENT_PADDING = 16.dp
private val COLOR_SWATCH_SIZE = 40.dp
private val COLOR_SWATCH_CORNER_RADIUS = 8.dp
private val SAVE_BUTTON_HEIGHT = 50.dp

@Composable
fun ThemePreviewBottomSheet(
    payload: ColorSchemePayload,
    onDismiss: () -> Unit,
    onSave: (UserGeneratedColorSchemeEntity) -> Unit
) {
    var themeName by remember { mutableStateOf(payload.name) }

    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Expanded,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PADDING_HORIZONTAL)
                .padding(bottom = PADDING_BOTTOM),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.theme_preview),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(SPACING_MEDIUM))

            OutlinedTextField(
                value = themeName,
                onValueChange = { themeName = it },
                label = { Text(text = stringResource(id = R.string.theme_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(SPACING_MEDIUM))

            val primary = parseHexColor(payload.primary)
            val secondary = parseHexColor(payload.secondary)
            val tertiary = parseHexColor(payload.tertiary)
            val surface = parseHexColor(payload.surface)
            val onSurface = parseHexColor(payload.onSurface)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PREVIEW_CARD_HEIGHT),
                shape = RoundedCornerShape(PREVIEW_CARD_CORNER_RADIUS),
                colors = CardDefaults.cardColors(containerColor = surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PREVIEW_CARD_CONTENT_PADDING),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = stringResource(id = R.string.preview),
                        color = onSurface,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Box(
                            modifier = Modifier
                                .size(COLOR_SWATCH_SIZE)
                                .clip(RoundedCornerShape(COLOR_SWATCH_CORNER_RADIUS))
                                .background(primary)
                        )
                        Box(
                            modifier = Modifier
                                .size(COLOR_SWATCH_SIZE)
                                .clip(RoundedCornerShape(COLOR_SWATCH_CORNER_RADIUS))
                                .background(secondary)
                        )
                        Box(
                            modifier = Modifier
                                .size(COLOR_SWATCH_SIZE)
                                .clip(RoundedCornerShape(COLOR_SWATCH_CORNER_RADIUS))
                                .background(tertiary)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SPACING_LARGE))

            Button(
                onClick = {
                    if (themeName.isNotBlank()) {
                        val entity = payload.toEntity().copy(name = themeName)
                        onSave(entity)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SAVE_BUTTON_HEIGHT)
            ) {
                Text(text = stringResource(id = R.string.save_theme))
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        Color(parseColor(if (hex.startsWith(COLOR_HEX_PREFIX)) hex else "$COLOR_HEX_PREFIX$hex"))
    } catch (e: Exception) {
        Color.Gray
    }
}
