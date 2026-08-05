package `in`.hridayan.ashell.ai.presentation.components.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import `in`.hridayan.ashell.ai.data.local.database.entity.ChatSessionEntity
import `in`.hridayan.ashell.ai.presentation.components.animatedcomposable.AnimatedStopIcon
import `in`.hridayan.ashell.ai.presentation.model.AiChatUiState
import `in`.hridayan.ashell.ai.presentation.viewmodel.AiChatViewModel
import `in`.hridayan.ashell.core.presentation.components.haptic.withHaptic
import `in`.hridayan.ashell.core.presentation.components.search.CustomSearchBar
import `in`.hridayan.ashell.core.resources.R
import kotlinx.coroutines.launch

@Composable
fun AiChatDrawerUI(
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    uiState: AiChatUiState,
    onClickSession: (String) -> Unit,
    onLongClickSession: (ChatSessionEntity) -> Unit,
    viewModel: AiChatViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val sessionSearchQuery by viewModel.sessionSearchQuery.collectAsState()

    ModalDrawerSheet(modifier = modifier) {
        Text(
            stringResource(R.string.ai_chat_sessions),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 32.dp),
            style = MaterialTheme.typography.titleLarge
        )

        // Added this spacer to absorb auto-focus of search bar
        Spacer(modifier = Modifier.focusable())

        CustomSearchBar(
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .fillMaxWidth(),
            value = sessionSearchQuery,
            onValueChange = { viewModel.onSessionSearchQueryChange(it) },
            hint = stringResource(R.string.ai_session_search_hint)
        )

        Spacer(modifier = Modifier.height(15.dp))

        NewChatButton(
            modifier = Modifier
                .padding(NavigationDrawerItemDefaults.ItemPadding)
                .fillMaxWidth(),
            onClick = withHaptic {
                viewModel.onNewChat()
                scope.launch { drawerState.close() }
            })

        Spacer(modifier = Modifier.height(15.dp))

        LazyColumn {
            items(uiState.sessions) { session ->
                @OptIn(ExperimentalFoundationApi::class)
                Surface(
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(50))
                        .combinedClickable(
                            onClick = withHaptic {
                                onClickSession(session.id)
                            },
                            onLongClick = withHaptic(HapticFeedbackType.LongPress) {
                                onLongClickSession(session)
                            }
                        )
                        .animateItem(),
                    shape = CircleShape,
                    color = if (session.id == uiState.currentSessionId) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.run {
                        if (session.id == uiState.currentSessionId) onSecondaryContainer else onSurfaceVariant
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(
                            session.title,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        val isGeneratingOrRunning =
                            uiState.generatingSessionIds.contains(session.id) ||
                                    uiState.runningTasks.any { it.sessionId == session.id }

                        if (isGeneratingOrRunning) {
                            Spacer(modifier = Modifier.width(8.dp))
                            AnimatedStopIcon(modifier = Modifier.size(16.dp))
                        } else if (session.isPinned) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewChatButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            modifier = Modifier.padding(vertical = 5.dp),
            imageVector = Icons.Rounded.Add,
            contentDescription = null
        )

        Spacer(Modifier.width(8.dp))

        Text(
            modifier = Modifier.padding(vertical = 5.dp),
            text = stringResource(R.string.ai_chat_new_chat),
            style = MaterialTheme.typography.titleMedium
        )
    }
}