package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.core.navigation.LocalNavController
import `in`.hridayan.ashell.core.navigation.navigateBack
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.scaffold.AppScaffold
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.settings.presentation.components.bottomsheet.ColorPickerBottomSheet
import `in`.hridayan.ashell.settings.presentation.page.lookandfeel.viewmodel.GenerateColorSchemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditColorSchemeScreen(
    themeId: Int,
    modifier: Modifier = Modifier,
    viewModel: GenerateColorSchemeViewModel = hiltViewModel()
) {
    val navController = LocalNavController.current
    val savedColorSchemes by viewModel.savedColorSchemes.collectAsState()

    // Find the theme being edited
    val initialTheme = remember(savedColorSchemes) {
        savedColorSchemes.find { it.id == themeId }
    }

    if (initialTheme == null) {
        // Theme not found or loading
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var editedTheme by remember { mutableStateOf(initialTheme) }
    var colorPickerState by remember { mutableStateOf<ColorPickerState?>(null) }

    val topAppBarState = rememberTopAppBarState()
    val listState = rememberLazyListState()

    AppScaffold(
        modifier = modifier.fillMaxSize(),
        onNavigateBack = { navController.navigateBack() },
        listState = listState,
        topAppBarState = topAppBarState,
        topBarTitle = editedTheme.name,
        fabContent = { expanded ->
            ExtendedFloatingActionButton(
                onClick = withHaptic {
                    viewModel.saveColorScheme(editedTheme)
                    navController.navigateBack()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = stringResource(R.string.save)
                    )
                },
                text = { Text(stringResource(R.string.save)) },
                expanded = expanded
            )
        },
        content = { innerPadding, scrollBehavior ->
            LazyColumn(
                state = listState,
                contentPadding = innerPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                item {
                    Spacer(modifier = Modifier.height(25.dp))
                }

                items(ColorRoleRegistry.registry) { pair ->
                    ColorRoleCard(
                        pair = pair,
                        theme = editedTheme,
                        onThemeChange = { editedTheme = it },
                        onOpenColorPicker = { roleIndex, color, onColorSelected ->
                            colorPickerState = ColorPickerState(color, onColorSelected)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    )

    // Show Bottom Sheet if state is not null
    colorPickerState?.let { state ->
        ColorPickerBottomSheet(
            initialColor = state.initialColor,
            onDismiss = { colorPickerState = null },
            onColorSelected = state.onColorSelected
        )
    }
}

data class ColorPickerState(
    val initialColor: Color,
    val onColorSelected: (Color) -> Unit
)

@Composable
fun ColorRoleCard(
    pair: ColorRolePair,
    theme: UserGeneratedColorScheme,
    onThemeChange: (UserGeneratedColorScheme) -> Unit,
    onOpenColorPicker: (roleIndex: Int, initialColor: Color, onSelected: (Color) -> Unit) -> Unit
) {
    val locale = LocalLocale.current
    val hex1 = pair.getRole1(theme)
    val hex2 = pair.getRole2(theme)
    val color1 = remember(hex1) { ColorRoleRegistry.parseHex(hex1) }
    val color2 = remember(hex2) { ColorRoleRegistry.parseHex(hex2) }

    val contrastRatio = remember(color1, color2) {
        ColorUtils.calculateContrast(color2.toArgb(), color1.toArgb())
    }
    val hasPoorContrast = contrastRatio < 4.5

    OutlinedCard(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = pair.titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                pair.previewComponent(color1, color2)
            }

            if (hasPoorContrast) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.low_contrast_ratio) + ": ${
                            String.format(
                                locale.platformLocale,
                                "%.1f",
                                contrastRatio
                            )
                        }:1",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // Role 1 Row
            ColorEditRow(
                label = stringResource(id = pair.role1NameRes),
                hex = hex1,
                color = color1,
                onClick = withHaptic(HapticFeedbackType.VirtualKey) {
                    onOpenColorPicker(1, color1) { newColor ->
                        val newHex = "#%06X".format(0xFFFFFF and newColor.toArgb())
                        onThemeChange(pair.setRole1(theme, newHex))
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role 2 Row
            ColorEditRow(
                label = stringResource(id = pair.role2NameRes),
                hex = hex2,
                color = color2,
                onClick = withHaptic {
                    onOpenColorPicker(2, color2) { newColor ->
                        val newHex = "#%06X".format(0xFFFFFF and newColor.toArgb())
                        onThemeChange(pair.setRole2(theme, newHex))
                    }
                }
            )
        }
    }
}

@Composable
fun ColorEditRow(
    label: String,
    hex: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = hex,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
