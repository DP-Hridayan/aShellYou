package `in`.hridayan.ashell.shell.presentation.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import `in`.hridayan.ashell.R
import `in`.hridayan.ashell.core.domain.model.SortType
import `in`.hridayan.ashell.core.presentation.components.text.AutoResizeableText
import `in`.hridayan.ashell.core.presentation.viewmodel.BookmarkViewModel

@Composable
fun BookmarkDialog(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    onSort: () -> Unit,
    onDismiss: () -> Unit,
    bookmarkViewModel: BookmarkViewModel = hiltViewModel()
) {
    val bookmarks = bookmarkViewModel.getAllBookmarks(SortType.AZ).value
    val bookmarkCount by bookmarkViewModel.getBookmarkCount.collectAsState(initial = 0)

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = modifier
                    .padding(24.dp)
                    .widthIn(min = 280.dp)
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.bookmarks) + " ($bookmarkCount)",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                LazyColumn(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .heightIn(max = 300.dp)
                ) {
                    items(bookmarks.size) { index ->
                        val bookmark = bookmarks[index]
                        Text(
                            text = bookmark.command,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
