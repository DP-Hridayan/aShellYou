package `in`.hridayan.ashell.logcat.presentation.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import `in`.hridayan.ashell.core.presentation.components.buttongroup.OverflowButtonGroup
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.model.ButtonConfigDefaults
import `in`.hridayan.ashell.core.presentation.model.ButtonGroupItem
import `in`.hridayan.ashell.core.presentation.model.ButtonType
import `in`.hridayan.ashell.core.resources.R
import `in`.hridayan.ashell.logcat.presentation.model.FilterEditorActions
import `in`.hridayan.ashell.logcat.presentation.model.FilterProfileForm

private val HorizontalPadding = 20.dp
private val BottomPadding = 24.dp

/** The fields of the filter profile editor, ending with Cancel and Save. */
@Composable
fun FilterEditorContent(
    form: FilterProfileForm,
    actions: FilterEditorActions,
    listState: LazyListState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        state = listState,
        contentPadding = PaddingValues(
            start = HorizontalPadding,
            end = HorizontalPadding,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + BottomPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(key = "name") { NameField(form, actions.onNameChange) }
        item(key = "mode") { FilterModeSelector(form.mode, actions.onModeChange) }
        item(key = "levels") { LevelsSection(form, actions) }
        item(key = "tags") {
            FilterValuesField(stringResource(R.string.tag), form.tags, actions.onTagsChange)
        }
        item(key = "packages") { PackagesField(form, actions) }
        item(key = "pids") {
            FilterValuesField(stringResource(R.string.logcat_filter_pid), form.pids, actions.onPidsChange)
        }
        item(key = "tids") {
            FilterValuesField(stringResource(R.string.logcat_filter_tid), form.tids, actions.onTidsChange)
        }
        item(key = "buttons") { EditorButtons(actions) }
    }
}

@Composable
private fun NameField(form: FilterProfileForm, onNameChange: (String) -> Unit) {
    val showError = form.showNameError && !form.isValid
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = form.name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.profile_name)) },
        isError = showError,
        supportingText = if (showError) {
            { Text(stringResource(R.string.profile_name_required)) }
        } else {
            null
        },
        singleLine = true,
    )
}

@Composable
private fun PackagesField(form: FilterProfileForm, actions: FilterEditorActions) {
    FilterValuesField(
        label = stringResource(R.string.packages),
        value = form.packages,
        onValueChange = actions.onPackagesChange,
        trailingIcon = {
            IconButton(onClick = withHaptic { actions.onPickApps() }) {
                Icon(
                    painter = painterResource(R.drawable.ic_package),
                    contentDescription = stringResource(R.string.choose_apps),
                )
            }
        },
    )
}

@Composable
private fun LevelsSection(form: FilterProfileForm, actions: FilterEditorActions) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.logcat_filter_level),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LogLevelChips(selected = form.levels, onToggle = actions.onLevelToggle)
    }
}

@Composable
private fun EditorButtons(actions: FilterEditorActions) {
    OverflowButtonGroup(
        modifier = Modifier.padding(top = 8.dp),
        items = listOf(
            ButtonGroupItem(
                buttonConfig = ButtonConfigDefaults.defaultConfig(type = ButtonType.OutlinedButton),
                text = stringResource(R.string.cancel),
                onClick = actions.onCancel,
            ),
            ButtonGroupItem(
                text = stringResource(R.string.save),
                onClick = actions.onSave,
            ),
        ),
    )
}
