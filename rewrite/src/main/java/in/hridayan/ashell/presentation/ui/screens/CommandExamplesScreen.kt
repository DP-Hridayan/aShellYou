package `in`.hridayan.ashell.presentation.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.presentation.ui.component.appbar.TopAppBarLarge
import `in`.hridayan.ashell.presentation.ui.component.command_examples.CommandItem
import `in`.hridayan.ashell.presentation.viewmodel.CommandViewModel

@Composable
fun CommandExamplesScreen(viewModel: CommandViewModel = hiltViewModel()) {
    TopAppBarLarge(
        modifier = Modifier,
        title = stringResource(R.string.commands),
        scrollContent = { innerPadding ->
            val commands by viewModel.allCommands.collectAsState(initial = emptyList())

            Button(modifier = Modifier.padding(top =500.dp), onClick = {
                viewModel.sortCommandsAlphabetically()
                Log.d("CommandExamplesScreen", "Button clicked!")
            }) {
                Text(text = "Test")
            }

            Button(modifier = Modifier.padding(top =700.dp), onClick = {
                viewModel.sortCommandsReversedAlphabetically()
                Log.d("CommandExamplesScreen", "Button clicked!")
            }) {
                Text(text = "Test")
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                items(commands.size) { index ->
                    CommandItem(text = commands[index].command)
                }
            }
        })

}
