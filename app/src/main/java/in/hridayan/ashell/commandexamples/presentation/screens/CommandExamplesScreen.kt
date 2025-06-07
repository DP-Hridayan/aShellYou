package `in`.hridayan.ashell.commandexamples.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.commandexamples.presentation.component.card.CommandItem
import `in`.hridayan.ashell.commandexamples.presentation.component.dialog.AddCommandDialog
import `in`.hridayan.ashell.commandexamples.presentation.viewmodel.CommandViewModel
import `in`.hridayan.ashell.core.presentation.components.appbar.TopAppBarLarge
import `in`.hridayan.ashell.core.presentation.ui.theme.Dimens


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandExamplesScreen(viewModel: CommandViewModel = hiltViewModel()) {

    var isDialogOpen by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBarLarge(
                topBarTitle = stringResource(id = R.string.commands),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier
                    .Companion
                    .padding(bottom = Dimens.paddingSmall),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = {
                    isDialogOpen = true
                }
            ) {
                val rotateAngle by animateFloatAsState(if (isDialogOpen) -45f else 0f)
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.rotate(rotateAngle)
                )
            }
        }
    ) {
        val commands by viewModel.allCommands.collectAsState(initial = emptyList())

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(vertical = Dimens.paddingMedium),
            verticalArrangement = Arrangement.spacedBy(Dimens.paddingMedium)
        ) {
            items(commands.size, key = { index -> commands[index].id }) { index ->
                CommandItem(
                    modifier = Modifier.animateItem(),
                    id = commands[index].id,
                    command = commands[index].command,
                    description = commands[index].description,
                    isFavourite = commands[index].isFavourite,
                    labels = commands[index].labels,
                )
            }
        }
    }

    if (isDialogOpen) AddCommandDialog(
        onDismiss = { isDialogOpen = false },
        modifier = Modifier
    )
}