package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme
import `in`.hridayan.ashell.core.resources.R

data class ColorRolePair(
    @StringRes val titleRes: Int,
    @StringRes val role1NameRes: Int,
    @StringRes val role2NameRes: Int,
    val getRole1: (UserGeneratedColorScheme) -> String,
    val getRole2: (UserGeneratedColorScheme) -> String,
    val setRole1: (UserGeneratedColorScheme, String) -> UserGeneratedColorScheme,
    val setRole2: (UserGeneratedColorScheme, String) -> UserGeneratedColorScheme,
    val previewComponent: @Composable (color1: Color, color2: Color) -> Unit
)

object ColorRoleRegistry {

    fun parseHex(hex: String): Color {
        return try {
            val hexStr = if (hex.startsWith("#")) hex else "#$hex"
            Color(hexStr.toColorInt())
        } catch (e: Exception) {
            Color.Gray
        }
    }

    val registry = listOf(
        // Primary
        ColorRolePair(
            titleRes = R.string.color_role_main_accent,
            role1NameRes = R.string.color_role_primary,
            role2NameRes = R.string.color_role_on_primary,
            getRole1 = { it.primary },
            getRole2 = { it.onPrimary },
            setRole1 = { theme, hex -> theme.copy(primary = hex) },
            setRole2 = { theme, hex -> theme.copy(onPrimary = hex) },
            previewComponent = { primary, onPrimary ->
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary,
                        contentColor = onPrimary
                    )
                ) {
                    Text(stringResource(R.string.color_preview_primary_button))
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_primary_container,
            role1NameRes = R.string.color_role_primary_container,
            role2NameRes = R.string.color_role_on_primary_container,
            getRole1 = { it.primaryContainer },
            getRole2 = { it.onPrimaryContainer },
            setRole1 = { theme, hex -> theme.copy(primaryContainer = hex) },
            setRole2 = { theme, hex -> theme.copy(onPrimaryContainer = hex) },
            previewComponent = { container, content ->
                FloatingActionButton(
                    onClick = {},
                    containerColor = container,
                    contentColor = content
                ) {
                    Text("+")
                }
            }
        ),

        // Secondary
        ColorRolePair(
            titleRes = R.string.color_role_secondary_accent,
            role1NameRes = R.string.color_role_secondary,
            role2NameRes = R.string.color_role_on_secondary,
            getRole1 = { it.secondary },
            getRole2 = { it.onSecondary },
            setRole1 = { theme, hex -> theme.copy(secondary = hex) },
            setRole2 = { theme, hex -> theme.copy(onSecondary = hex) },
            previewComponent = { secondary, onSecondary ->
                SuggestionChip(
                    onClick = {},
                    label = { Text(stringResource(R.string.color_role_secondary)) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = secondary,
                        labelColor = onSecondary
                    )
                )
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_secondary_container,
            role1NameRes = R.string.color_role_secondary_container,
            role2NameRes = R.string.color_role_on_secondary_container,
            getRole1 = { it.secondaryContainer },
            getRole2 = { it.onSecondaryContainer },
            setRole1 = { theme, hex -> theme.copy(secondaryContainer = hex) },
            setRole2 = { theme, hex -> theme.copy(onSecondaryContainer = hex) },
            previewComponent = { container, content ->
                FilledTonalButton(
                    onClick = {},
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = container,
                        contentColor = content
                    )
                ) {
                    Text(stringResource(R.string.color_preview_tonal_button))
                }
            }
        ),

        // Tertiary
        ColorRolePair(
            titleRes = R.string.color_role_tertiary_accent,
            role1NameRes = R.string.color_role_tertiary,
            role2NameRes = R.string.color_role_on_tertiary,
            getRole1 = { it.tertiary },
            getRole2 = { it.onTertiary },
            setRole1 = { theme, hex -> theme.copy(tertiary = hex) },
            setRole2 = { theme, hex -> theme.copy(onTertiary = hex) },
            previewComponent = { tertiary, onTertiary ->
                Badge(containerColor = tertiary, contentColor = onTertiary) {
                    Text(stringResource(R.string._new))
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_tertiary_container,
            role1NameRes = R.string.color_role_tertiary_container,
            role2NameRes = R.string.color_role_on_tertiary_container,
            getRole1 = { it.tertiaryContainer },
            getRole2 = { it.onTertiaryContainer },
            setRole1 = { theme, hex -> theme.copy(tertiaryContainer = hex) },
            setRole2 = { theme, hex -> theme.copy(onTertiaryContainer = hex) },
            previewComponent = { container, content ->
                Box(
                    modifier = Modifier
                        .size(64.dp, 32.dp)
                        .background(container, shape = MaterialTheme.shapes.large),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.tab), color = content)
                }
            }
        ),

        // Error
        ColorRolePair(
            titleRes = R.string.color_role_error,
            role1NameRes = R.string.color_role_error,
            role2NameRes = R.string.color_role_on_error,
            getRole1 = { it.error },
            getRole2 = { it.onError },
            setRole1 = { theme, hex -> theme.copy(error = hex) },
            setRole2 = { theme, hex -> theme.copy(onError = hex) },
            previewComponent = { error, onError ->
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = error,
                        contentColor = onError
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_error_container,
            role1NameRes = R.string.color_role_error_container,
            role2NameRes = R.string.color_role_on_error_container,
            getRole1 = { it.errorContainer },
            getRole2 = { it.onErrorContainer },
            setRole1 = { theme, hex -> theme.copy(errorContainer = hex) },
            setRole2 = { theme, hex -> theme.copy(onErrorContainer = hex) },
            previewComponent = { container, content ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = container,
                        contentColor = content
                    )
                ) {
                    Text(
                        stringResource(R.string.color_preview_error_occurred),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        ),

        // Core Surfaces
        ColorRolePair(
            titleRes = R.string.color_role_app_background,
            role1NameRes = R.string.color_role_background,
            role2NameRes = R.string.color_role_on_background,
            getRole1 = { it.background },
            getRole2 = { it.onBackground },
            setRole1 = { theme, hex -> theme.copy(background = hex) },
            setRole2 = { theme, hex -> theme.copy(onBackground = hex) },
            previewComponent = { bg, onBg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg)
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.color_preview_screen_content), color = onBg)
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_surface,
            role1NameRes = R.string.color_role_surface,
            role2NameRes = R.string.color_role_on_surface,
            getRole1 = { it.surface },
            getRole2 = { it.onSurface },
            setRole1 = { theme, hex -> theme.copy(surface = hex) },
            setRole2 = { theme, hex -> theme.copy(onSurface = hex) },
            previewComponent = { surface, onSurface ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surface)
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.color_preview_surface_area), color = onSurface)
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_surface_variant,
            role1NameRes = R.string.color_role_surface_variant,
            role2NameRes = R.string.color_role_on_surface_variant,
            getRole1 = { it.surfaceVariant },
            getRole2 = { it.onSurfaceVariant },
            setRole1 = { theme, hex -> theme.copy(surfaceVariant = hex) },
            setRole2 = { theme, hex -> theme.copy(onSurfaceVariant = hex) },
            previewComponent = { variant, onVariant ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(variant)
                        .padding(16.dp)
                ) {
                    Text(stringResource(R.string.color_preview_list_item), color = onVariant)
                }
            }
        ),

        // Surface Containers
        ColorRolePair(
            titleRes = R.string.color_role_surface_container_lowest,
            role1NameRes = R.string.color_role_surface_container_lowest,
            role2NameRes = R.string.color_role_on_surface,
            getRole1 = { it.surfaceContainerLowest },
            getRole2 = { it.onSurface },
            setRole1 = { theme, hex -> theme.copy(surfaceContainerLowest = hex) },
            setRole2 = { theme, hex -> theme.copy(onSurface = hex) },
            previewComponent = { container, content ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = container,
                        contentColor = content
                    )
                ) {
                    Text(
                        stringResource(R.string.color_preview_lowest_elevation),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_surface_container_low,
            role1NameRes = R.string.color_role_surface_container_low,
            role2NameRes = R.string.color_role_on_surface,
            getRole1 = { it.surfaceContainerLow },
            getRole2 = { it.onSurface },
            setRole1 = { theme, hex -> theme.copy(surfaceContainerLow = hex) },
            setRole2 = { theme, hex -> theme.copy(onSurface = hex) },
            previewComponent = { container, content ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = container,
                        contentColor = content
                    )
                ) {
                    Text(
                        stringResource(R.string.color_preview_low_elevation),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_surface_container,
            role1NameRes = R.string.color_role_surface_container,
            role2NameRes = R.string.color_role_on_surface,
            getRole1 = { it.surfaceContainer },
            getRole2 = { it.onSurface },
            setRole1 = { theme, hex -> theme.copy(surfaceContainer = hex) },
            setRole2 = { theme, hex -> theme.copy(onSurface = hex) },
            previewComponent = { container, content ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = container,
                        contentColor = content
                    )
                ) {
                    Text(
                        stringResource(R.string.color_preview_standard_elevation),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_surface_container_high,
            role1NameRes = R.string.color_role_surface_container_high,
            role2NameRes = R.string.color_role_on_surface,
            getRole1 = { it.surfaceContainerHigh },
            getRole2 = { it.onSurface },
            setRole1 = { theme, hex -> theme.copy(surfaceContainerHigh = hex) },
            setRole2 = { theme, hex -> theme.copy(onSurface = hex) },
            previewComponent = { container, content ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = container,
                        contentColor = content
                    )
                ) {
                    Text(
                        stringResource(R.string.color_preview_high_elevation),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_surface_container_highest,
            role1NameRes = R.string.color_role_surface_container_highest,
            role2NameRes = R.string.color_role_on_surface,
            getRole1 = { it.surfaceContainerHighest },
            getRole2 = { it.onSurface },
            setRole1 = { theme, hex -> theme.copy(surfaceContainerHighest = hex) },
            setRole2 = { theme, hex -> theme.copy(onSurface = hex) },
            previewComponent = { container, content ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = container,
                        contentColor = content
                    )
                ) {
                    Text(
                        stringResource(R.string.color_preview_highest_elevation),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        ),

        // Borders & Scrim
        ColorRolePair(
            titleRes = R.string.color_role_outline,
            role1NameRes = R.string.color_role_outline,
            role2NameRes = R.string.color_role_surface, // Paired with surface for preview
            getRole1 = { it.outline },
            getRole2 = { it.surface },
            setRole1 = { theme, hex -> theme.copy(outline = hex) },
            setRole2 = { theme, hex -> theme.copy(surface = hex) },
            previewComponent = { outline, surface ->
                OutlinedButton(
                    onClick = {},
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, outline)
                ) {
                    Text(stringResource(R.string.color_preview_outlined_button), color = outline)
                }
            }
        ),
        ColorRolePair(
            titleRes = R.string.color_role_outline_variant,
            role1NameRes = R.string.color_role_outline_variant,
            role2NameRes = R.string.color_role_surface,
            getRole1 = { it.outlineVariant },
            getRole2 = { it.surface },
            setRole1 = { theme, hex -> theme.copy(outlineVariant = hex) },
            setRole2 = { theme, hex -> theme.copy(surface = hex) },
            previewComponent = { outlineVariant, surface ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(surface)
                        .padding(16.dp)
                ) {
                    HorizontalDivider(color = outlineVariant, thickness = 2.dp)
                }
            }
        )
    )
}
