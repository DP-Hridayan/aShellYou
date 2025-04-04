package `in`.hridayan.ashell.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.presentation.ui.component.command_examples.AddCommandDialog
import `in`.hridayan.ashell.presentation.ui.component.command_examples.CommandItem
import `in`.hridayan.ashell.presentation.viewmodel.CommandViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandExamplesScreen(viewModel: CommandViewModel = hiltViewModel()) {

    var isDialogOpen by rememberSaveable { mutableStateOf(false) }

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    Text(
                        text = stringResource(id = R.string.commands),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {},
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier,
                onClick = {
                    isDialogOpen = true
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    ) { innerPadding ->
        val commands by viewModel.allCommands.collectAsState(initial = emptyList())

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(commands.size) { index ->
                CommandItem(
                    text = commands[index].command +
                            commands[index].description +
                            commands[index].example +
                            commands[index].useCount +
                            commands[index].isFavourite
                )
            }
        }
    }

    if (isDialogOpen) AddCommandDialog(onDismiss = { isDialogOpen = false }, modifier = Modifier)
}
