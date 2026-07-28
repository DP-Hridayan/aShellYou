package `in`.hridayan.ashell.settings.presentation.page.lookandfeel.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.theme.domain.model.UserGeneratedColorScheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexEditorSection(
    theme: UserGeneratedColorScheme,
    onSave: (UserGeneratedColorScheme) -> Unit,
    onCancel: () -> Unit
) {
    var editingTheme by remember(theme) { mutableStateOf(theme) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = `in`.hridayan.ashell.core.resources.R.string.edit_theme_title, theme.name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        val colors = listOf(
            stringResource(id = `in`.hridayan.ashell.core.resources.R.string.primary) to editingTheme.primary to { it: String -> editingTheme = editingTheme.copy(primary = it) },
           stringResource(id = `in`.hridayan.ashell.core.resources.R.string.on_primary) to editingTheme.onPrimary to { it: String -> editingTheme = editingTheme.copy(onPrimary = it) },
            stringResource(id = `in`.hridayan.ashell.core.resources.R.string.primary_container) to editingTheme.primaryContainer to { it: String -> editingTheme = editingTheme.copy(primaryContainer = it) },
            stringResource(id = `in`.hridayan.ashell.core.resources.R.string.on_primary_container) to editingTheme.onPrimaryContainer to { it: String -> editingTheme = editingTheme.copy(onPrimaryContainer = it) },
            stringResource(id = `in`.hridayan.ashell.core.resources.R.string.secondary) to editingTheme.secondary to { it: String -> editingTheme = editingTheme.copy(secondary = it) },
            androidx.compose.ui.res.stringResource(id = `in`.hridayan.ashell.core.resources.R.string.on_secondary) to editingTheme.onSecondary to { it: String -> editingTheme = editingTheme.copy(onSecondary = it) },
            androidx.compose.ui.res.stringResource(id = `in`.hridayan.ashell.core.resources.R.string.tertiary) to editingTheme.tertiary to { it: String -> editingTheme = editingTheme.copy(tertiary = it) },
            androidx.compose.ui.res.stringResource(id = `in`.hridayan.ashell.core.resources.R.string.on_tertiary) to editingTheme.onTertiary to { it: String -> editingTheme = editingTheme.copy(onTertiary = it) },
            androidx.compose.ui.res.stringResource(id = `in`.hridayan.ashell.core.resources.R.string.background) to editingTheme.background to { it: String -> editingTheme = editingTheme.copy(background = it) },
            androidx.compose.ui.res.stringResource(id = `in`.hridayan.ashell.core.resources.R.string.on_background) to editingTheme.onBackground to { it: String -> editingTheme = editingTheme.copy(onBackground = it) },
            androidx.compose.ui.res.stringResource(id = `in`.hridayan.ashell.core.resources.R.string.surface) to editingTheme.surface to { it: String -> editingTheme = editingTheme.copy(surface = it) },
            androidx.compose.ui.res.stringResource(id = `in`.hridayan.ashell.core.resources.R.string.on_surface) to editingTheme.onSurface to { it: String -> editingTheme = editingTheme.copy(onSurface = it) }
        )

        colors.forEach { (labelValuePair, onValueChange) ->
            val (label, value) = labelValuePair
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onCancel) {
                Text(androidx.compose.ui.res.stringResource(id = `in`.hridayan.ashell.core.resources.R.string.cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onSave(editingTheme) }) {
                Text(androidx.compose.ui.res.stringResource(id = `in`.hridayan.ashell.core.resources.R.string.save_changes))
            }
        }
    }
}
